package com.example.leafcheck

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import org.json.JSONObject
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Response
import java.util.Date
import android.Manifest
import com.google.firebase.auth.FirebaseAuth


class AddTree : AppCompatActivity() {

    private lateinit var btnCapture: Button
    private lateinit var btnSubmit: Button
    private lateinit var imgPreview: ImageView
    private lateinit var namaPohon: EditText

    //System
    private var capturedImageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val CAMERA_PERMISSION_CODE = 101
    private val API_URL = "http://192.168.1.2:8000/predict"
    private val fAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tree)

        btnCapture = findViewById(R.id.uploadImgButton)
        btnSubmit = findViewById(R.id.addTreeButton)
        imgPreview = findViewById(R.id.leafPhoto)
        namaPohon = findViewById(R.id.treeAddName)

        btnCapture.setOnClickListener {
            checkCameraPermission()
        }

        btnSubmit.setOnClickListener {
            capturedImageUri?.let { uri ->
                uploadImage(uri)
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }


    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera() // Permission granted, open camera
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgPreview.setImageBitmap(imageBitmap)

            // Convert bitmap to URI
            capturedImageUri = saveImageToFile(imageBitmap)
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

    private fun uploadImage(imageUri: Uri) {
        val file = getFileFromUri(imageUri)
        if (file == null || !file.exists() || file.length() == 0L) {
            showErrorDialog("File Error", "File does not exist, is empty, or could not be found.")
            return
        }

        Log.d("File Info", "File exists: ${file.exists()}, Size: ${file.length()} bytes")

        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())

        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val request = Request.Builder()
            .url(API_URL)
            .post(MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(body)
                .build()
            )
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("API Error", "Failed to upload image: ${e.message}")
                    Toast.makeText(this@AddTree, "Tidak ada Respon", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@AddTree, "Respon API berhasil di dapatkan", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val responseData = response.body?.string() ?: "{}"
                Log.d("API Response", responseData)
                parseAndUploadResponse(responseData, imageUri)
            }
        })
    }

    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun goToMain(imageUri: Uri) {
        val intent = Intent(this, LeafCheck::class.java)
        startActivity(intent)
    }

    private fun parseAndUploadResponse(responseData: String, imageUri: Uri) {
        try {
            val jsonObject = JSONObject(responseData)

            val leaf = jsonObject.optString("leaf", "Unknown")
            val keterangan = jsonObject.optString("keterangan", "No Description")
            val leaftype = jsonObject.optString("leaftype", "Unknown")

            // Upload data to Firestore
            val db = FirebaseFirestore.getInstance()
            val treeData = hashMapOf(
                "condDate" to Date(),
                "treeCond" to leaf,
                "treeDesc" to keterangan,
                "treeName" to namaPohon.text.toString(),
                "treeType" to leaftype.toInt(),
                "owner" to (fAuth.currentUser?.email ?: "Unknown"),
                "favorite" to false
            )

            db.collection("Trees")
                .add(treeData)
                .addOnSuccessListener { documentReference ->
                    Log.d("Firestore", "Document added with ID: ${documentReference.id}")
                    runOnUiThread {
                        goToMain(imageUri)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore Error", "Error adding document", e)
                    runOnUiThread {
                        showErrorDialog("Firestore Error", "Failed to save data: ${e.message}")
                    }
                }
        } catch (e: Exception) {
            Log.e("JSON Parse Error", "Failed to parse response: ${e.message}")
            runOnUiThread {
                showErrorDialog("Parse Error", "Failed to parse server response: ${e.message}")
            }
        }
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
                showErrorDialog("File Error", "Failed to process image: ${e.message}")
            }
            return null
        }
        return file
    }
}