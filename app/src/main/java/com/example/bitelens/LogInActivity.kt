package com.example.bitelens

//not working


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LogInActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonGoToRegister: Button
    private lateinit var buttonLoginWithGoogle: SignInButton

    private val googleSignInRequestCode = 234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        editTextEmail = findViewById(R.id.editTextEmailLogin)
        editTextPassword = findViewById(R.id.editTextPasswordLogin)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister)
        buttonLoginWithGoogle = findViewById(R.id.buttonLoginGoogle)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login success
                            Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT).show()
                            // Intent to navigate to main activity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            // If login fails, display a message to the user.
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }

        buttonGoToRegister.setOnClickListener {
            // Navigate to the Register Activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this,gso)

        buttonLoginWithGoogle.setOnClickListener{
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, googleSignInRequestCode)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            googleSignInRequestCode -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                    
                }catch (e:ApiException){
                    e.printStackTrace()
                }
            }
        }
    }
private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnSuccessListener { authResult ->
            val newUser = authResult.additionalUserInfo?.isNewUser == true

            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("user_name", account.displayName ?: "")
                putExtra("user_email", account.email ?: "")
                putExtra("user_photo_url", account.photoUrl.toString())
            }

            if (newUser) {
                createUserInDatabase(account)
            }

            startActivity(intent)
            finish()
        }
        .addOnFailureListener {
            Toast.makeText(this, "Authentication failed due to: ${it.message}", Toast.LENGTH_LONG).show()
        }
}

    private fun createUserInDatabase(account: GoogleSignInAccount) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userProfile = UserProfile(
            name = account.displayName ?: "",
            email = account.email ?: "",
            age = 18,  // Default age, consider allowing the user to set this later
            gender = "prefare not to say",  // Default gender, consider allowing the user to set this later
            avatarUri = account.photoUrl.toString()  // Default to Google profile picture


        )
        val dbInstance = getString(R.string.DB_ref)
        val databaseReference = FirebaseDatabase.getInstance(dbInstance).getReference("Users")
        databaseReference.child(userId).setValue(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "User profile created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

    }


}
