package com.example.leafcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var retypepassword: EditText
    private lateinit var registButton: Button
    private lateinit var loginTextReturn: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        retypepassword = findViewById(R.id.retypepassword)
        registButton = findViewById(R.id.registerButton)
        loginTextReturn = findViewById(R.id.loginTextReturn)

        firebaseAuth = FirebaseAuth.getInstance()

        registButton.setOnClickListener {
            registerUser()
        }

        loginTextReturn.setOnClickListener {
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }

    private fun registerUser() {
        val mail = email.text.toString()
        val pass = password.text.toString()
        val repass = retypepassword.text.toString()

        if (mail.isNotEmpty() && pass.isNotEmpty() && repass.isNotEmpty() && mail.contains("@")) {
            if (pass == repass) {
                val db = FirebaseFirestore.getInstance()

                // Check if email already exists in Firestore
                db.collection("UserProfiles")
                    .whereEqualTo("email", mail)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            // Email doesn't exist, proceed with Firebase Authentication
                            firebaseAuth.createUserWithEmailAndPassword(mail, pass)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Store user data in Firestore
                                        val user = hashMapOf(
                                            "email" to mail,
                                            "firstTime" to true
                                        )

                                        db.collection("UserProfiles")
                                            .add(user) // Save user data to Firestore
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(this, Login::class.java)
                                                startActivity(intent)
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // Email already exists in Firestore
                            Toast.makeText(this, "Email sudah terdaftar!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal memeriksa email: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
            }
        }

    }
}