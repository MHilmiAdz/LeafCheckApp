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

class LeafCheck : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var myTreeAdapter: RecyclerAdapter
    private lateinit var treeList: ArrayList<TreeData>
    private lateinit var dbTree: FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaf_check)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        treeList = arrayListOf()
        myTreeAdapter = RecyclerAdapter(treeList)
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

    private fun EventChangeListener(){
        dbTree = FirebaseFirestore.getInstance()
        dbTree.collection("Trees").orderBy("treeName", Query.Direction.ASCENDING).
                addSnapshotListener(object : EventListener<QuerySnapshot>{
                    override fun onEvent(
                        value: QuerySnapshot?,
                        error: FirebaseFirestoreException?
                    ) {
                        if (error != null){
                            Log.e("Firestore Error", error.message.toString())
                            return
                        }

                        for (dc : DocumentChange in value?.documentChanges!!){
                            if (dc.type == DocumentChange.Type.ADDED){
                                treeList.add(dc.document.toObject(TreeData::class.java))
                            }
                        }
                        myTreeAdapter.notifyDataSetChanged()
                    }
                })
    }
}