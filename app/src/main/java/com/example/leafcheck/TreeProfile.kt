package com.example.leafcheck

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class TreeProfile : AppCompatActivity() {

    private lateinit var imgTree: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tree_profile)

        imgTree = findViewById(R.id.leafImg)

        val imageUri = intent.getStringExtra("imageUri")
        if (imageUri != null) {
            imgTree.setImageURI(Uri.parse(imageUri))
        }
    }
}
