package com.example.bitelens

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bitelens.ml.GoogleFoods
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CameraFragment : Fragment() {
    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private lateinit var textureView: TextureView
    private var handler: Handler? = null
    private lateinit var model: GoogleFoods
    private lateinit var imageProcessor: ImageProcessor
    private var isDetecting: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textureView = view.findViewById(R.id.textureView)
        model = GoogleFoods.newInstance(requireContext())
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()
        initializeCamera()

        val buttonAddManual = view.findViewById<Button>(R.id.buttonAddFoodManually)
        buttonAddManual.setOnClickListener {
            showManualEntryDialog()
        }
    }

    private fun initializeCamera() {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                if (!isDetecting) return
                processImage(surface)
            }
        }
    }

    private fun processImage(surface: SurfaceTexture) {
        val bitmap = textureView.bitmap ?: return
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)
        val outputs = model.process(image)
        val probabilities = outputs.probabilityAsCategoryList
        val mostLikelyCategory = probabilities.maxByOrNull { it.score }
        val label = mostLikelyCategory?.label ?: "Unknown"
        val confidence = mostLikelyCategory?.score ?: 0f
        if (mostLikelyCategory != null && confidence > 0.5) {
            isDetecting = false
            activity?.runOnUiThread {
                showAddDialog(label, confidence)
            }
        }
    }

    private fun showAddDialog(label: String, confidence: Float) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Confirm Detection")
            setMessage("Detected: $label with ${String.format("%.2f", confidence * 100)}% confidence. Add to list?")
            setPositiveButton("Yes") { dialog, which ->
                fetchAndAddFood(label)
                Toast.makeText(context, "$label added to list", Toast.LENGTH_SHORT).show()
                isDetecting = true  // Resume detecting

            }
            setNegativeButton("No") { dialog, which ->
                Toast.makeText(context, "Not added", Toast.LENGTH_SHORT).show()
                isDetecting = true  // Resume detecting
            }
            setCancelable(false)
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
    }

    override fun onPause() {
        stopBackgroundThread()
        super.onPause()
    }

    private fun startBackgroundThread() {
        val thread = HandlerThread("CameraBackground").also { it.start() }
        handler = Handler(thread.looper)
    }

    private fun stopBackgroundThread() {
        handler?.looper?.quitSafely()
        handler = null
    }
    private fun openCamera() {
        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        startPreview()
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        camera.close()
                    }
                }, handler)
            }else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPreview() {
        val surfaceTexture = textureView.surfaceTexture!!
        surfaceTexture.setDefaultBufferSize(1920, 1080)
        val surface = Surface(surfaceTexture)

        val previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder?.addTarget(surface)

        cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                previewRequestBuilder?.let { session.setRepeatingRequest(it.build(), null, handler) }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Toast.makeText(context, "Failed to configure camera.", Toast.LENGTH_SHORT).show()
            }
        }, handler)
    }

private fun fetchAndAddFood(name: String) {
    val apiKey = getString(R.string.api_key)
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.calorieninjas.com/v1/nutrition?query=$name")
        .get()
        .addHeader("X-Api-Key", apiKey)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Failed to fetch data: $e", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Request failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val itemsArray = json.getJSONArray("items")
                        if (itemsArray.length() > 0) {
                            val item = itemsArray.getJSONObject(0)
                            val foodData = FoodData(
                                id = UUID.randomUUID().toString(),
                                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                                name = item.optString("name", "Unknown"),
                                calories = item.optDouble("calories", 0.0),
                                servingSizeG = item.optDouble("serving_size_g", 0.0),
                                fatTotalG = item.optDouble("fat_total_g", 0.0),
                                fatSaturatedG = item.optDouble("fat_saturated_g", 0.0),
                                proteinG = item.optDouble("protein_g", 0.0),
                                sodiumMg = item.optDouble("sodium_mg", 0.0),
                                potassiumMg = item.optDouble("potassium_mg", 0.0),
                                cholesterolMg = item.optDouble("cholesterol_mg", 0.0),
                                carbohydratesTotalG = item.optDouble("carbohydrates_total_g", 0.0),
                                fiberG = item.optDouble("fiber_g", 0.0),
                                sugarG = item.optDouble("sugar_g", 0.0)
                            )
                            pushDataToFirebase(foodData)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Error parsing JSON data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    })
}

    private fun pushDataToFirebase(foodData: FoodData) {
        val databaseReference = FirebaseDatabase.getInstance("https://bitelens-90db4-default-rtdb.europe-west1.firebasedatabase.app").getReference("Foods")
        databaseReference.child(foodData.id).setValue(foodData)
            .addOnSuccessListener {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Data successfully saved to Firebase!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to save data to Firebase: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, open camera
                    openCamera()
                } else {
                    // Permission denied, show a message or handle it accordingly
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showManualEntryDialog() {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.dialog_add_food_manually, null)
        val foodNameEditText = view.findViewById<EditText>(R.id.editTextFoodName)

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Add Food Manually")
            setView(view)
            setPositiveButton("Add") { dialog, _ ->
                val foodName = foodNameEditText.text.toString()
                if (foodName.isNotEmpty()) {
                    fetchAndAddFood(foodName)
                } else {
                    Toast.makeText(context, "Please enter a food name", Toast.LENGTH_SHORT).show()
                }
            }

            setNegativeButton("Cancel", null)
            show()
        }
    }

}

