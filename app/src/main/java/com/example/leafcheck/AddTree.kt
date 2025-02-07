package com.example.leafcheck

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
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
import java.util.Date

class AddTree : AppCompatActivity() {

    private lateinit var btnCapture: Button
    private lateinit var btnSubmit: Button
    private lateinit var imgPreview: ImageView
    private lateinit var namaPohon: EditText
    private var capturedImageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val API_URL = "http://192.168.1.7:8000/predict"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tree)

        btnCapture = findViewById(R.id.uploadImgButton)
        btnSubmit = findViewById(R.id.addTreeButton)
        imgPreview = findViewById(R.id.leafPhoto)
        namaPohon = findViewById(R.id.treeAddName)

        btnCapture.setOnClickListener {
            openCamera()
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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
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
        val file = getFileFromUri(imageUri)  // Convert Uri to File
        if (file == null || !file.exists() || file.length() == 0L) {
            Log.e("File Error", "File does not exist, is empty, or could not be found.")
            return
        }

        Log.d("File Info", "File exists: ${file.exists()}, Size: ${file.length()} bytes")

        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())

        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)  // Adjust key name if needed

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
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Upload Error", "Failed to send image: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseData = response.body?.string() ?: "{}"
                Log.d("API Response", responseData)  // Print full response
                // Parse response and upload to Firebase
                parseAndUploadResponse(responseData, imageUri)
            }
        })
    }

    private fun goToTreeProfile(imageUri: Uri) {
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
                "treeType" to leaftype.toInt()
            )

            db.collection("Trees")
                .add(treeData)
                .addOnSuccessListener { documentReference ->
                    Log.d("Firestore", "Document added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore Error", "Error adding document", e)
                }
        } catch (e: Exception) {
            Log.e("JSON Parse Error", "Failed to parse response: ${e.message}")
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

            // Debugging: Check file existence
            Log.d("File Conversion", "File created: ${file.exists()}, Size: ${file.length()} bytes")

        } catch (e: IOException) {
            Log.e("File Conversion Error", "Failed to convert Uri to File: ${e.message}")
            return null
        }
        return file
    }
}
