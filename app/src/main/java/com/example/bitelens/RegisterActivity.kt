package com.example.bitelens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonGoToLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextName = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin)

        buttonRegister.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword == password) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Registration Successful.", Toast.LENGTH_SHORT).show()
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                createUserInDatabase(user, name)
                            }
                            val intent = Intent(this, LogInActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                if (confirmPassword != password) {
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter all details.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonGoToLogin.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }
    }

    private fun createUserInDatabase(user: FirebaseUser, name: String) {
        val userProfile = UserProfile(
            name = name,
            email = user.email ?: "",
            age = 0,  // Default age, consider allowing the user to set this later
            gender = "",  // Default gender, consider allowing the user to set this later
            avatarUri = user.photoUrl?.toString() ?: ""  // Default to empty string if no photo URL
        )
        val dbInstance = getString(R.string.DB_ref)
        val databaseReference = FirebaseDatabase.getInstance(dbInstance).getReference("Users")
        databaseReference.child(user.uid).setValue(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "User profile created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
