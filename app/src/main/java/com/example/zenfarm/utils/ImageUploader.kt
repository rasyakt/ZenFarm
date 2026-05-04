package com.example.zenfarm.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

object ImageUploader {
    private val storage = FirebaseStorage.getInstance()
    
    /**
     * Upload image to Firebase Storage
     * @param context Android context
     * @param imageUri Local file URI or path
     * @param folder Folder name in Storage (e.g., "hewan", "silsilah")
     * @return Download URL of uploaded image
     */
    suspend fun uploadImage(context: Context, imageUri: String, folder: String = "hewan"): String {
        if (imageUri.isEmpty()) {
            throw IllegalArgumentException("Image URI tidak boleh kosong")
        }
        
        // Generate unique filename
        val fileName = "${folder}_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("$folder/$fileName")
        
        // Convert string path to Uri
        val uri = if (imageUri.startsWith("content://")) {
            Uri.parse(imageUri)
        } else {
            Uri.fromFile(File(imageUri))
        }
        
        // Upload file
        val uploadTask = storageRef.putFile(uri).await()
        
        // Get download URL
        val downloadUrl = storageRef.downloadUrl.await()
        return downloadUrl.toString()
    }
    
    /**
     * Delete image from Firebase Storage
     * @param imageUrl Full download URL of the image
     */
    suspend fun deleteImage(imageUrl: String) {
        if (imageUrl.isEmpty() || !imageUrl.contains("firebase")) {
            return // Not a Firebase Storage URL
        }
        
        try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            // Ignore if file doesn't exist
            e.printStackTrace()
        }
    }
}
