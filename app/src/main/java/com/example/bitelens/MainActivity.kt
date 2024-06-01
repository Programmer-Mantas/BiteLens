// MainActivity.kt
package com.example.bitelens

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SettingsFragment.OnSettingsSaveListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var databaseReference: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        setupFirebase()
        updateNavigationHeader()
    }

    private fun setupFirebase() {
        userId = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance("https://bitelens-90db4-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
    }

    private fun updateNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val profilePicImageView = headerView.findViewById<ImageView>(R.id.navigation_bar_profile_pic)
        val usernameTextView = headerView.findViewById<TextView>(R.id.navigation_bar_profile_username)
        val emailTextView = headerView.findViewById<TextView>(R.id.navigation_bar_profile_email)

        userId?.let {
            databaseReference.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(UserProfile::class.java)
                    userProfile?.let { profile ->
                        usernameTextView.text = profile.name.ifBlank { "Default Username" }
                        emailTextView.text = profile.email.ifBlank { "email@example.com" }
                        val avatarUri = profile.avatarUri.takeIf { it.isNotBlank() } ?: ""
                        if (avatarUri.isNotBlank()) {
                            Picasso.get().load(avatarUri).into(profilePicImageView)
                        } else {
                            profilePicImageView.setImageResource(R.drawable.defaultavatarprofile)
                        }
                    } ?: run {
                        usernameTextView.text = "Default Username"
                        emailTextView.text = "email@example.com"
                        profilePicImageView.setImageResource(R.drawable.defaultavatarprofile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    usernameTextView.text = "Default Username"
                    emailTextView.text = "email@example.com"
                    profilePicImageView.setImageResource(R.drawable.defaultavatarprofile)
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        updateNavigationHeader() // Update the header each time the activity is resumed
    }

    override fun onSettingsSaved() {
        updateNavigationHeader()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_settings -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment()).commit()
            R.id.nav_camera -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CameraFragment()).commit()
            R.id.nav_info -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InfoFragment()).commit()
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish()  // Ensure this activity is closed so back press does not return to a logged-in state
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
