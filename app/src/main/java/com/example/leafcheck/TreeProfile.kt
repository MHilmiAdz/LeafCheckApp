package com.example.leafcheck

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

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
    private var capturedImageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 2
    private val API_URL = "http://192.168.1.2:8000/predict"

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
            val intent = Intent(this, LeafCheck::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Ensures TreeProfile is closed
        }

        favButton.setOnClickListener {
            val treeId = intent.getStringExtra("treeId") ?: return@setOnClickListener
            val treeRef = dbTree.collection("Trees").document(treeId)

            treeRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val isFavorite = document.getBoolean("favorite") ?: false
                    treeRef.update("favorite", !isFavorite)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Favorite updated successfully")
                            Toast.makeText(this, if (!isFavorite) "Added to favorites" else "Removed from favorites", Toast.LENGTH_SHORT).show()
                            updateFavoriteButtonUI(!isFavorite) // **Panggil fungsi untuk update UI**
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore Error", "Failed to update favorite", e)
                            Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("Firestore", "Tree document not found")
                    Toast.makeText(this, "Tree not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.e("Firestore Error", "Failed to retrieve tree", e)
                Toast.makeText(this, "Error retrieving tree", Toast.LENGTH_SHORT).show()
            }
        }

        updateHealthButton.setOnClickListener {
            openCamera()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LeafCheck::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Ensure TreeProfile is removed
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Convert bitmap to URI
            capturedImageUri = saveImageToFile(imageBitmap)
        }

        capturedImageUri?.let { uri ->
            uploadImage(uri)
        }
    }

    private fun saveImageToFile(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "captured_image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(file)
    }

    private fun getFileFromUri(uri: Uri): File? {
        val file = File(cacheDir, "upload_image.jpg")
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            Log.d("File Conversion", "File created: ${file.exists()}, Size: ${file.length()} bytes")

        } catch (e: IOException) {
            Log.e("File Conversion Error", "Failed to convert Uri to File: ${e.message}")
            runOnUiThread {
                Toast.makeText(this@TreeProfile, "Tidak ada Respon", Toast.LENGTH_SHORT).show()
            }
            return null
        }
        return file
    }

    private fun uploadImage(imageUri: Uri) {
        val file = getFileFromUri(imageUri)
        if (file == null || !file.exists() || file.length() == 0L) {
            Toast.makeText(this@TreeProfile, "Tidak ada Respon", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("File Info", "File exists: ${file.exists()}, Size: ${file.length()} bytes")

        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())

        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val request = Request.Builder()
            .url(API_URL)
            .post(
                MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body)
                .build()
            )
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TreeProfile, "Tidak ada Respon", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@TreeProfile, "Respon API berhasil di dapatkan", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val responseData = response.body?.string() ?: "{}"
                Log.d("API Response", responseData)
                parseAndUploadResponse(responseData)

            }
        })
    }

    private fun loadTreeData(treeId: String) {
        dbTree.collection("Trees").document(treeId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val treeData = document.toObject(TreeData::class.java)
                    treeNameTextView.text = treeData?.treeName ?: "Unknown"
                    treeTypeTextView.text = when (treeData?.treeType) {
                        1 -> "Apel"
                        2 -> "Mangga"
                        3 -> "Jeruk"
                        else -> "Unknown"
                    }
                    treeConditionTextView.text = treeData?.treeCond ?: "No condition available"
                    treeDescriptionTextView.text = treeData?.treeDesc ?: "No description available"
                    treeDateHealth.text = (treeData?.condDate ?: "No date available").toString()

                    // **🔄 Ambil status favorit dari Firestore**
                    val isFavorite = document.getBoolean("favorite") ?: false
                    updateFavoriteButtonUI(isFavorite) // Panggil fungsi untuk memperbarui tombol
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

    private fun parseAndUploadResponse(responseData: String) {
        try {
            val jsonObject = JSONObject(responseData)
            val leaf = jsonObject.optString("leaf", "Unknown")
            val keterangan = jsonObject.optString("keterangan", "No Description")
            val leaftype = jsonObject.optString("leaftype", "Unknown").toIntOrNull() ?: -1

            val treeId = intent.getStringExtra("treeId") ?: return

            dbTree.collection("Trees").document(treeId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val currentTreeType = document.getLong("treeType")?.toInt() ?: -1

                        if (currentTreeType != leaftype) {
                            Log.e("Update Error", "Tree type mismatch, update aborted.")
                            runOnUiThread {
                                Toast.makeText(this@TreeProfile, "Jenis daun berbeda", Toast.LENGTH_SHORT).show()
                            }
                            return@addOnSuccessListener
                        }

                        val updateData = hashMapOf(
                            "condDate" to Date(),
                            "treeCond" to leaf,
                            "treeDesc" to keterangan
                        )

                        dbTree.collection("Trees").document(treeId)
                            .update(updateData as Map<String, Any>)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Document updated successfully")
                                goToTreeProfile(treeId)
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore Error", "Error updating document", e)
                                runOnUiThread {
                                    Toast.makeText(this@TreeProfile, "Update kesehatan gagal", Toast.LENGTH_SHORT).show()
                                    // 🚀 **Refresh UI here**
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore Error", "Failed to retrieve tree document", e)
                    runOnUiThread {
                        Toast.makeText(this@TreeProfile, "API tidak berjalan", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("JSON Parse Error", "Failed to parse response: ${e.message}")
            runOnUiThread {
                Toast.makeText(this@TreeProfile, "Gagal mengupload foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToTreeProfile(treeId: String) {
        val intent = Intent(this, TreeProfile::class.java).apply {
            putExtra("treeId", treeId) // Pass only the document ID
        }
        startActivity(intent)
    }

    private fun updateFavoriteButtonUI(isFavorite: Boolean) {
        if (isFavorite) {
            favButton.background.setTint(getColor(R.color.Leafcolor)) // Warna hijau jika favorit
            favButton.setTextColor(getColor(R.color.white))
        } else {
            favButton.background.setTint(getColor(R.color.white)) // Warna putih jika tidak favorit
            favButton.setTextColor(getColor(R.color.black))
        }
    }

}
