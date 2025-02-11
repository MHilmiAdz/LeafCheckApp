package com.example.leafcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfile : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonUbahData: Button
    private lateinit var buttonKeluar: Button
    private lateinit var labelNama: TextView
    private lateinit var labelEmail: TextView
    private lateinit var ppView: ImageView

    //System
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        recyclerView = findViewById(R.id.recyclerView)
        buttonUbahData = findViewById(R.id.profil_tombolEdit)
        buttonKeluar = findViewById(R.id.profil_tombolKeluar)
        labelNama = findViewById(R.id.profil_labelViewNama)
        labelEmail = findViewById(R.id.profil_labelViewEmail)
        ppView = findViewById(R.id.profil_imageView)

        firebaseAuth = FirebaseAuth.getInstance()

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Handle profile item click
                    goToMain()
                    true
                }
                else -> false
            }
        }

        buttonUbahData.setOnClickListener {
            goToUpdateProfile()
        }

        buttonKeluar.setOnClickListener {
            firebaseAuth.signOut()
            goToLogin()
        }

        val currentUser = firebaseAuth.currentUser
        val db = FirebaseFirestore.getInstance()

        if (currentUser != null) {
            val email = currentUser.email
            labelEmail.text = email  // Display the email in UI

            db.collection("UserProfiles")
                .whereEqualTo("userEmail", email)  // Use currentUser.email
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]  // Get the first document
                        val username = document.getString("userName") ?: "Unknown User"
                        labelNama.text = username  // Display username in UI
                    } else {
                        labelNama.text = "Username not found"
                    }
                }
                .addOnFailureListener { e ->
                    labelNama.text = "Error loading username"
                    Log.e("Firestore", "Error fetching username", e)
                }
        }
    }

    private fun goToMain() {
        val intent = Intent(this, LeafCheck::class.java)
        startActivity(intent)
    }

    private fun goToUpdateProfile() {
        val intent = Intent(this, UpdateProfile::class.java)
        startActivity(intent)
    }

    private fun goToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
}