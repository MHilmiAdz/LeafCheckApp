package com.example.leafcheck

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class TreeProfile : AppCompatActivity() {

    // INITIALIZE LAYOUT
    private lateinit var treeNameTextView: TextView
    private lateinit var treeTypeTextView: TextView
    private lateinit var treeConditionTextView: TextView
    private lateinit var treeDescriptionTextView: TextView
    private lateinit var backButton: Button
    private lateinit var favButton: Button
    private lateinit var updateHealthButton: Button
    private lateinit var treeDateHealth: TextView

    // INITIALIZE SYSTEM
    private lateinit var dbTree: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tree_profile)

        treeNameTextView = findViewById(R.id.nameTree)
        treeTypeTextView = findViewById(R.id.typeTree)
        treeConditionTextView = findViewById(R.id.conditionTree)
        treeDescriptionTextView = findViewById(R.id.descTree)
        treeDateHealth = findViewById(R.id.dateTree)
        backButton = findViewById(R.id.backbutton)
        favButton = findViewById(R.id.favButton)
        updateHealthButton = findViewById(R.id.updateHealth)

        dbTree = FirebaseFirestore.getInstance()

        val treeId = intent.getStringExtra("treeId")
        if (treeId != null) {
            loadTreeData(treeId)
        }

        backButton.setOnClickListener {
            finish()
        }

        favButton.setOnClickListener {
            // Handle favorite button click
        }

        updateHealthButton.setOnClickListener {
            updateTreeData(dbTree.collection("Trees").document(treeId.toString()).toString(), mapOf())
        }
    }

    private fun loadTreeData(treeId: String) {
        dbTree.collection("Trees").document(treeId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val treeData = document.toObject(TreeData::class.java)
                    treeNameTextView.text = treeData?.treeName ?: "Unknown"
                    treeTypeTextView.text = when (treeData?.treeType) {
                        0,1 -> "Apel"
                        2,3 -> "Mangga"
                        4,5 -> "Jeruk"
                        else -> "Unknown"
                    }
                    treeConditionTextView.text = treeData?.treeCond ?: "No condition available"
                    treeDescriptionTextView.text = treeData?.treeDesc ?: "No description available"
                    treeDateHealth.text = (treeData?.condDate ?: "No date available").toString()
                } else {
                    Log.e("TreeProfile", "Document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TreeProfile", "Error fetching data", e)
            }
    }

    private fun updateTreeData(treeId: String, updatedData: Map<String, Any>) {
        dbTree.collection("Trees").document(treeId).update(updatedData)
            .addOnSuccessListener {
                Log.d("TreeProfile", "Data updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("TreeProfile", "Error updating data", e)
            }
    }
}
