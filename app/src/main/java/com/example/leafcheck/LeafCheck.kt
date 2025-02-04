package com.example.leafcheck

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LeafCheck : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaf_check)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Handle profile item click
                    goToProfile()
                    true
                }
                else -> false
            }
        }

        // Set default selection
        bottomNavigationView.selectedItemId = R.id.nav_home
    }

    // Function to navigate to the Main activity


    private fun goToProfile() {
        val intent = Intent(this, UserProfile::class.java)
        startActivity(intent)
    }

    private fun goToTreeProfile() {
        val intent = Intent(this, TreeProfile::class.java)
        startActivity(intent)
    }

    private fun goToAddTree(){

    }
}