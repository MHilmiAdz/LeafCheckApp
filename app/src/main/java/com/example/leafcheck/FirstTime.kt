package com.example.leafcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirstTime : AppCompatActivity() {

    private lateinit var firstUsername: EditText
    private lateinit var addUserNameButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_first_time)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firstUsername = findViewById(R.id.addUserName)
        addUserNameButton = findViewById(R.id.addUserNameButton)
        firebaseAuth = FirebaseAuth.getInstance()

        addUserNameButton.setOnClickListener {
            val username = firstUsername.text.toString()
            val db = FirebaseFirestore.getInstance()
            val userEmail = firebaseAuth.currentUser?.email

            if (username.isNotEmpty() && userEmail != null) {
                // Find the user's existing document in Firestore using their email
                db.collection("UserProfiles")
                    .whereEqualTo("userEmail", userEmail)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.documents[0] // Get the first document
                            val userRef = document.reference

                            // Update the username and set firstTime to false
                            userRef.update(
                                mapOf(
                                    "userName" to username,
                                    "firstTime" to false
                                )
                            ).addOnSuccessListener {
                                Log.d("Firestore", "Username updated successfully")
                                runOnUiThread {
                                    Toast.makeText(this, "Pembuatan username berhasil", Toast.LENGTH_SHORT).show()
                                    goToMainLayout()
                                }
                            }.addOnFailureListener { e ->
                                Log.e("Firestore Error", "Error updating document", e)
                                runOnUiThread {
                                    Toast.makeText(this, "Gagal memperbarui username", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // If no existing user is found, show an error
                            Toast.makeText(this, "Pengguna tidak ditemukan di database", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore Error", "Error getting document", e)
                        Toast.makeText(this, "Terjadi kesalahan saat mencari pengguna", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMainLayout(){
        val intent = Intent(this, LeafCheck::class.java)
        startActivity(intent)
    }
}