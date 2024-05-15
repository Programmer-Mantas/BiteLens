package com.example.bitelens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bitelens.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var databaseReference: DatabaseReference
    private var userId: String? = null
    private var currentAvatarUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        setupFirebase()
        loadUserProfile()
        setupAgeSpinner()
        setupAvatarPicker()
        binding.buttonSaveChanges.setOnClickListener { saveUserProfile() }
        return binding.root
    }

    private fun setupFirebase() {
        userId = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance("https://bitelens-90db4-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
    }

    private fun loadUserProfile() {
        userId?.let {
            databaseReference.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(UserProfile::class.java)
                    userProfile?.let { profile ->
                        populateProfile(profile)
                    } ?: Toast.makeText(context, "No user profile found.", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun populateProfile(userProfile: UserProfile) {
        binding.editTextName.setText(userProfile.name)
        binding.editTextEmail.setText(userProfile.email)
        binding.spinnerAge.setSelection(userProfile.age - 18)  // Assuming age starts from 18

        when (userProfile.gender) {
            "male" -> binding.radioGroupGender.check(R.id.radioButtonMale)
            "female" -> binding.radioGroupGender.check(R.id.radioButtonFemale)
            "prefer not to say" -> binding.radioGroupGender.check(R.id.radioButtonOther)
        }

        userProfile.avatarUri.takeIf { it.isNotBlank() }?.let { url ->
            currentAvatarUri = url
            Picasso.get().load(url).resize(400, 400).centerCrop().into(binding.imageViewAvatar)
        } ?: run {
            Picasso.get().load(R.drawable.defaultavatarprofile).resize(400, 400).centerCrop().into(binding.imageViewAvatar)
        }
    }

    private fun saveUserProfile() {
        val name = binding.editTextName.text.toString()
        val email = binding.editTextEmail.text.toString()
        val age = binding.spinnerAge.selectedItem.toString().toInt()
        val gender = when (binding.radioGroupGender.checkedRadioButtonId) {
            R.id.radioButtonMale -> "male"
            R.id.radioButtonFemale -> "female"
            R.id.radioButtonOther -> "prefer not to say"
            else -> ""
        }

        val userProfile = UserProfile(name, email, age, gender, currentAvatarUri ?: "")
        userId?.let {
            databaseReference.child(it).setValue(userProfile).addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAgeSpinner() {
        val ages = (18..100).toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ages)
        binding.spinnerAge.adapter = adapter
    }

    private fun setupAvatarPicker() {
        binding.buttonSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
        binding.imageViewAvatar.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let { uri ->
                uploadImageToFirebase(uri)
            }
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("User/$userId/profile_picture.jpg")
        storageRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    currentAvatarUri = uri.toString()
                    Picasso.get().load(uri).resize(400, 400).centerCrop().into(binding.imageViewAvatar)
                    saveUserProfile()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
