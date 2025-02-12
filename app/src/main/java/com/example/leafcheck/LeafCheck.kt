package com.example.leafcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.auth.FirebaseAuth

class LeafCheck : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var myTreeAdapter: RecyclerAdapter
    private lateinit var treeList: ArrayList<TreeData>

    //System
    private lateinit var dbTree: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaf_check)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        treeList = ArrayList()
        myTreeAdapter = RecyclerAdapter(treeList) { treeId ->
            goToTreeProfile(treeId)
        }
        recyclerView.adapter = myTreeAdapter
        dbTree = FirebaseFirestore.getInstance()

        EventChangeListener()

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

        fab.setOnClickListener {
            goToAddTree()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // Closes all activities and exits the app
    }

    // Function to navigate to the Main activity

    private fun goToTreeProfile(treeId: String) {
        val intent = Intent(this, TreeProfile::class.java).apply {
            putExtra("treeId", treeId) // Pass only the document ID
        }
        startActivity(intent)
    }

    private fun goToProfile() {
        val intent = Intent(this, UserProfile::class.java)
        startActivity(intent)
    }

    private fun goToAddTree(){
        val intent = Intent(this, AddTree::class.java)
        startActivity(intent)
    }

    private fun EventChangeListener() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email

        if (userEmail == null) {
            Log.e("Firestore Error", "User not logged in")
            return
        }

        dbTree.collection("Trees")
            .whereEqualTo("owner", userEmail) // 🔥 Filter trees by the owner's email
            .orderBy("treeName", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }

                treeList.clear() // 🔥 Prevent duplicate entries
                for (dc in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val treeData = dc.document.toObject(TreeData::class.java)
                        treeData.treeId = dc.document.id
                        treeList.add(treeData)
                    }
                }
                myTreeAdapter.notifyDataSetChanged()
            }
    }
}