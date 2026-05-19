package com.example.zenfarm.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUploader {
    
    /**
     * Convert image to Base64 string (untuk disimpan di Firestore)
     * @param context Android context
     * @param imageUri Local file URI (content:// or file://)
     * @param maxSizeKB Maximum size in KB (default 500KB)
     * @return Base64 string of compressed image
     */
    suspend fun convertImageToBase64(
        context: Context, 
        imageUri: String, 
        maxSizeKB: Int = 500
    ): String {
        if (imageUri.isEmpty()) {
            throw IllegalArgumentException("Image URI tidak boleh kosong")
        }
        
        android.util.Log.d("ImageUploader", "Converting image to Base64 from URI: $imageUri")
        
        try {
            val uri = Uri.parse(imageUri)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            
            if (inputStream == null) {
                throw IllegalArgumentException("Cannot access image URI: $imageUri")
            }
            
            // Decode image
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) {
                throw IllegalArgumentException("Cannot decode image")
            }
            
            android.util.Log.d("ImageUploader", "Original size: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Compress image
            val compressedBitmap = compressBitmap(originalBitmap, maxSizeKB)
            
            // Convert to Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            
            val sizeKB = byteArray.size / 1024
            android.util.Log.d("ImageUploader", "Compressed size: $sizeKB KB")
            android.util.Log.d("ImageUploader", "Base64 length: ${base64String.length}")
            
            // Cleanup
            originalBitmap.recycle()
            compressedBitmap.recycle()
            
            return base64String
            
        } catch (e: Exception) {
            android.util.Log.e("ImageUploader", "Conversion failed: ${e.message}", e)
            throw Exception("Gagal memproses foto: ${e.message ?: "Unknown error"}")
        }
    }
    
    /**
     * Compress bitmap to target size
     */
    private fun compressBitmap(bitmap: Bitmap, maxSizeKB: Int): Bitmap {
        val maxWidth = 800
        val maxHeight = 800
        
        var width = bitmap.width
        var height = bitmap.height
        
        // Calculate scale to fit within max dimensions
        val scale = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height,
            1.0f
        )
        
        if (scale < 1.0f) {
            width = (width * scale).toInt()
            height = (height * scale).toInt()
        }
        
        android.util.Log.d("ImageUploader", "Resizing to: ${width}x${height}")
        
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
    
    /**
     * Legacy method - kept for compatibility
     * Now converts to Base64 instead of uploading to Firebase Storage
     */
    suspend fun uploadImage(context: Context, imageUri: String, folder: String = "hewan"): String {
        android.util.Log.d("ImageUploader", "Using Base64 storage (Firebase Storage not available)")
        return convertImageToBase64(context, imageUri)
    }
    
    /**
     * Delete image - no-op for Base64 storage
     */
    suspend fun deleteImage(imageUrl: String) {
        // No-op: Base64 images are stored in Firestore, deleted with document
        android.util.Log.d("ImageUploader", "Delete not needed for Base64 storage")
    }
}