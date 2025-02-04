package com.example.leafcheck

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {

    // UI Elements
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotpassbutton: TextView
    private lateinit var signupText: TextView
    private lateinit var googleauth: ImageView

    // Firebase & Google Sign-In
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        forgotpassbutton = findViewById(R.id.forgotpassbutton)
        signupText = findViewById(R.id.signupText)
        googleauth = findViewById(R.id.googlesignin)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

//        // Check if user is already signed in
//        val currentUser = firebaseAuth.currentUser
//        if (currentUser != null) {
//            goToMain() // Skip login and go to main screen
//            return
//        }

        // Configure Google Sign-In
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.frost_webclient_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Handle login button click
        loginButton.setOnClickListener {
            loginUser()
        }

        // Handle signup click
        signupText.setOnClickListener {
            goToRegister()
        }

        // Google Sign-In click
        googleauth.setOnClickListener {
            signInWithGoogle()
        }
    }

    // Activity Result API for Google Sign-In
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(this, "Google Sign-In failed!", Toast.LENGTH_SHORT).show()
            }
        }

    // Function to navigate to the Register activity
    private fun goToRegister() {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    // Function to navigate to the Main activity
    private fun goToMain() {
        val intent = Intent(this, LeafCheck::class.java)
        startActivity(intent)
    }

    // Function to handle login user
    private fun loginUser() {
        val mail = email.text.toString()
        val pass = password.text.toString()

        if (TextUtils.isEmpty(mail)) {
            email.error = "Email tidak boleh kosong!"
            return
        }
        if (TextUtils.isEmpty(pass)) {
            password.error = "Password tidak boleh kosong!"
            return
        }
        if (pass.length < 6) {
            password.error = "Password harus lebih dari 6 karakter!"
            return
        }

        firebaseAuth.signInWithEmailAndPassword(mail, pass)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Toast.makeText(this@Login, "Login berhasil!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Toast.makeText(this@Login, "Login gagal!", Toast.LENGTH_SHORT).show()
                    goToMain()
                }
            }
    }

    // Function to sign in with Google
    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Authenticate Google Sign-In with Firebase
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login dengan Google berhasil!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Toast.makeText(this, "Login dengan Google gagal!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
