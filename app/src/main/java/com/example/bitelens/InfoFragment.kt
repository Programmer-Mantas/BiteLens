package com.example.bitelens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InfoFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        queryFoodData()

        view.findViewById<Button>(R.id.buttonSendEmail).setOnClickListener {
            sendEmail()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    private fun queryFoodData() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentMonth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val databaseRef = FirebaseDatabase.getInstance("https://bitelens-90db4-default-rtdb.europe-west1.firebasedatabase.app").getReference("Foods")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalCalories = 0.0
                var totalFatTotalG = 0.0
                var totalFatSaturatedG = 0.0
                var totalProteinG = 0.0
                var totalSodiumMg = 0.0
                var totalPotassiumMg = 0.0
                var totalCholesterolMg = 0.0
                var totalCarbohydratesTotalG = 0.0
                var totalFiberG = 0.0
                var totalSugarG = 0.0

                snapshot.children.forEach { child ->
                    val foodItem = child.getValue(FoodData::class.java)
                    if (foodItem != null && foodItem.userId == currentUserId && foodItem.date.startsWith(currentMonth)) {
                        totalCalories += foodItem.calories
                        totalFatTotalG += foodItem.fatTotalG
                        totalFatSaturatedG += foodItem.fatSaturatedG
                        totalProteinG += foodItem.proteinG
                        totalSodiumMg += foodItem.sodiumMg
                        totalPotassiumMg += foodItem.potassiumMg
                        totalCholesterolMg += foodItem.cholesterolMg
                        totalCarbohydratesTotalG += foodItem.carbohydratesTotalG
                        totalFiberG += foodItem.fiberG
                        totalSugarG += foodItem.sugarG
                    }
                }

                // Update the UI with the summed values
                view?.findViewById<TextView>(R.id.foodCaloriesTextView)?.text = String.format("%.1f", totalCalories)
                view?.findViewById<TextView>(R.id.foodFatTotalTextView)?.text = String.format("%.1f", totalFatTotalG)
                view?.findViewById<TextView>(R.id.foodFatSaturatedTextView)?.text = String.format("%.1f", totalFatSaturatedG)
                view?.findViewById<TextView>(R.id.foodProteinTextView)?.text = String.format("%.1f", totalProteinG)
                view?.findViewById<TextView>(R.id.foodSodiumTextView)?.text = String.format("%.1f", totalSodiumMg)
                view?.findViewById<TextView>(R.id.foodPotassiumTextView)?.text = String.format("%.1f", totalPotassiumMg)
                view?.findViewById<TextView>(R.id.foodCholesterolTextView)?.text = String.format("%.1f", totalCholesterolMg)
                view?.findViewById<TextView>(R.id.foodCarbohydratesTextView)?.text = String.format("%.1f", totalCarbohydratesTotalG)
                view?.findViewById<TextView>(R.id.foodFiberTextView)?.text = String.format("%.1f", totalFiberG)
                view?.findViewById<TextView>(R.id.foodSugarTextView)?.text = String.format("%.1f", totalSugarG)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun sendEmail() {
        val recipient = "" // Customize or dynamically set the recipient
        val subject = "Your Monthly Food Summary"
        val message = buildEmailBody()

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(context, "No email application found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildEmailBody(): String {
        val calories = view?.findViewById<TextView>(R.id.foodCaloriesTextView)?.text.toString()
        val fatTotal = view?.findViewById<TextView>(R.id.foodFatTotalTextView)?.text.toString()
        val fatSaturated = view?.findViewById<TextView>(R.id.foodFatSaturatedTextView)?.text.toString()
        val protein = view?.findViewById<TextView>(R.id.foodProteinTextView)?.text.toString()
        val sodium = view?.findViewById<TextView>(R.id.foodSodiumTextView)?.text.toString()
        val potassium = view?.findViewById<TextView>(R.id.foodPotassiumTextView)?.text.toString()
        val cholesterol = view?.findViewById<TextView>(R.id.foodCholesterolTextView)?.text.toString()
        val carbohydrates = view?.findViewById<TextView>(R.id.foodCarbohydratesTextView)?.text.toString()
        val fiber = view?.findViewById<TextView>(R.id.foodFiberTextView)?.text.toString()
        val sugar = view?.findViewById<TextView>(R.id.foodSugarTextView)?.text.toString()

        return buildString {
            append("Here is your food intake summary for the month:\n")
            append("Calories: $calories kcal\n")
            append("Total Fat: $fatTotal g\n")
            append("Saturated Fat: $fatSaturated g\n")
            append("Protein: $protein g\n")
            append("Sodium: $sodium mg\n")
            append("Potassium: $potassium mg\n")
            append("Cholesterol: $cholesterol mg\n")
            append("Carbohydrates: $carbohydrates g\n")
            append("Fiber: $fiber g\n")
            append("Sugar: $sugar g\n")
            append("\nMore details about nutritional value can be found: https://pressbooks.oer.hawaii.edu/humannutrition/chapter/comparing-diets/.")
        }
    }


}


