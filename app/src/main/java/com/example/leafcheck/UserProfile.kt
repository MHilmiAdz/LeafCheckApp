package com.example.leafcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var treeAdapter: RecyclerAdapter
    private var treeList = ArrayList<TreeData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        recyclerView = findViewById(R.id.recyclerView)
        buttonUbahData = findViewById(R.id.profil_tombolEdit)
        buttonKeluar = findViewById(R.id.profil_tombolKeluar)
        labelNama = findViewById(R.id.profil_labelViewNama)
        labelEmail = findViewById(R.id.profil_labelViewEmail)
        ppView = findViewById(R.id.profil_imageView)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView.layoutManager = LinearLayoutManager(this)
        treeAdapter = RecyclerAdapter(treeList) { treeId -> goToTreeProfile(treeId) }
        recyclerView.adapter = treeAdapter

        // Load user profile & favorite trees
        loadUserProfile()
        loadFavoriteTrees()

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    goToMain()
                    true
                }
                else -> false
            }
        }

        buttonUbahData.setOnClickListener { goToUpdateProfile() }
        buttonKeluar.setOnClickListener {
            firebaseAuth.signOut()
            goToLogin()
        }
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser ?: return
        labelEmail.text = currentUser.email

        db.collection("UserProfiles")
            .whereEqualTo("userEmail", currentUser.email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    labelNama.text = document.getString("userName") ?: "Unknown User"
                } else {
                    labelNama.text = "Username not found"
                }
            }
            .addOnFailureListener { e ->
                labelNama.text = "Error loading username"
                Log.e("Firestore", "Error fetching username", e)
            }
    }

    private fun loadFavoriteTrees() {
        val currentUser = firebaseAuth.currentUser ?: return

        db.collection("Trees")
            .whereEqualTo("favorite", true)
            .whereEqualTo("owner", currentUser.email) // Filter by user email
            .get()
            .addOnSuccessListener { documents ->
                treeList.clear()
                for (document in documents) {
                    val treeData = document.toObject(TreeData::class.java)
                    treeData.treeId = document.id
                    treeList.add(treeData)
                }
                treeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error loading favorite trees", e)
            }
    }

    private fun goToTreeProfile(treeId: String) {
        val intent = Intent(this, TreeProfile::class.java).apply {
            putExtra("treeId", treeId)
        }
        startActivity(intent)
    }

    private fun goToMain() {
        startActivity(Intent(this, LeafCheck::class.java))
    }

    private fun goToUpdateProfile() {
        startActivity(Intent(this, UpdateProfile::class.java))
    }

    private fun goToLogin() {
        startActivity(Intent(this, Login::class.java))
    }
}
