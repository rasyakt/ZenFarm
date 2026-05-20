package com.example.zenfarm.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Utility untuk decode Base64 string ke ImageBitmap
 * Digunakan untuk menampilkan gambar yang disimpan sebagai Base64 di Firestore
 */
object Base64ImageDecoder {
    
    private const val TAG = "Base64ImageDecoder"
    
    /**
     * Decode Base64 string ke Bitmap
     * Supports:
     * - Plain Base64 string
     * - Data URI format (data:image/jpeg;base64,...)
     */
    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        if (base64String.isEmpty()) {
            Log.w(TAG, "Empty Base64 string")
            return null
        }
        
        Log.d(TAG, "=== DECODE START ===")
        Log.d(TAG, "Input length: ${base64String.length}")
        Log.d(TAG, "First 50 chars: ${base64String.take(50)}")
        Log.d(TAG, "Last 50 chars: ${base64String.takeLast(50)}")
        
        return try {
            // Remove data URI prefix if present
            val cleanBase64 = if (base64String.startsWith("data:image")) {
                Log.d(TAG, "Detected data URI format")
                // Extract Base64 part after "base64,"
                val parts = base64String.split(",")
                if (parts.size >= 2) {
                    Log.d(TAG, "Extracted Base64 part (length: ${parts[1].length})")
                    parts[1]
                } else {
                    Log.e(TAG, "Invalid data URI format - no comma separator")
                    return null
                }
            } else {
                Log.d(TAG, "Plain Base64 format")
                base64String
            }
            
            // Remove any whitespace or newlines
            val trimmedBase64 = cleanBase64.replace("\\s".toRegex(), "")
            Log.d(TAG, "After trim length: ${trimmedBase64.length}")
            
            // Decode Base64 to byte array
            val decodedBytes = Base64.decode(trimmedBase64, Base64.DEFAULT)
            Log.d(TAG, "Decoded to ${decodedBytes.size} bytes")
            
            // Decode byte array to Bitmap
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            
            if (bitmap != null) {
                Log.d(TAG, "✅ Bitmap created successfully: ${bitmap.width}x${bitmap.height}")
                Log.d(TAG, "=== DECODE SUCCESS ===")
            } else {
                Log.e(TAG, "❌ Failed to create bitmap from bytes")
                Log.e(TAG, "Bytes might not be valid image data")
                Log.d(TAG, "=== DECODE FAILED ===")
            }
            
            bitmap
            
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "❌ Invalid Base64 string: ${e.message}")
            Log.e(TAG, "This usually means the string contains invalid Base64 characters")
            Log.d(TAG, "=== DECODE FAILED ===")
            null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error decoding Base64: ${e.message}", e)
            Log.d(TAG, "=== DECODE FAILED ===")
            null
        }
    }
    
    /**
     * Decode Base64 string ke ImageBitmap (untuk Compose)
     */
    fun decodeBase64ToImageBitmap(base64String: String): ImageBitmap? {
        val bitmap = decodeBase64ToBitmap(base64String)
        return bitmap?.asImageBitmap()
    }
}

/**
 * Composable helper untuk remember decoded Base64 image
 */
@Composable
fun rememberBase64Image(base64String: String): ImageBitmap? {
    return remember(base64String) {
        android.util.Log.d("rememberBase64Image", "Called with string length: ${base64String.length}")
        if (base64String.isEmpty()) {
            android.util.Log.w("rememberBase64Image", "Empty string provided")
            null
        } else {
            val result = Base64ImageDecoder.decodeBase64ToImageBitmap(base64String)
            android.util.Log.d("rememberBase64Image", "Result: ${if (result != null) "SUCCESS" else "FAILED"}")
            result
        }
    }
}
