package com.example.leafcheck

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UpdateProfile : AppCompatActivity() {

    private lateinit var updateUsername: EditText
    private lateinit var backButton: ImageButton
    private lateinit var submitButton: Button

    //System
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updateUsername = findViewById(R.id.editprofil_kolomEditUsername)
        submitButton = findViewById(R.id.editprofil_tombolSimpanData)
        backButton = findViewById(R.id.backButtontoHome)
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        val db = FirebaseFirestore.getInstance()

        backButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val newUsername = updateUsername.text.toString().trim()

            if (currentUser != null && newUsername.isNotEmpty()) {
                val email = currentUser.email

                db.collection("UserProfiles")
                    .whereEqualTo("userEmail", email)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val documentId = documents.documents[0].id // Get the first matching document
                            db.collection("UserProfiles").document(documentId)
                                .update("userName", newUsername)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Username updated successfully!", Toast.LENGTH_SHORT).show()
                                    finish() // Close the activity after update
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show()
                                    Log.e("Firestore", "Error updating username", e)
                                }
                        } else {
                            Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "Error fetching user document", e)
                    }
            } else {
                Toast.makeText(this, "Please enter a new username", Toast.LENGTH_SHORT).show()
            }
        }
    }
}