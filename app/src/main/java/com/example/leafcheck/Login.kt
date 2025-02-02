package com.example.leafcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Login : AppCompatActivity() {

    private lateinit var loginText: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotpassbutton: TextView
    private lateinit var signupText: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginText = findViewById(R.id.loginText)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        forgotpassbutton = findViewById(R.id.forgotpassbutton)
        signupText = findViewById(R.id.signupText)
        imageView = findViewById(R.id.imageView)

        loginButton.setOnClickListener {
            // Handle login button click
            goToMain()
        }
//        forgotpassbutton.setOnClickListener {
//            // Handle forgot password click
//            goToForgotPassword()
//        }
        signupText.setOnClickListener {
            // Handle signup click
            goToRegister()
        }
    }

    // Function to navigate to the Register activity
    private fun goToRegister() {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    // Function to navigate to the ForgotPassword activity
//    private fun goToForgotPassword() {
//        val intent = Intent(this, ForgotPassword::class.java)
//        startActivity(intent)
//    }

    // Function to navigate to the Main activity
    private fun goToMain() {
        val intent = Intent(this, LeafCheck::class.java)
        startActivity(intent)
    }
}