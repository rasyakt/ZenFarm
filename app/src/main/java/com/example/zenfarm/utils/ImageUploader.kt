package com.example.zenfarm.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUploader {

    private const val TAG = "ImageUploader"

    /**
     * Convert image to Base64 string (untuk disimpan di Firestore)
     * Supports: file paths, file:// URIs, content:// URIs
     * 
     * @param context Android context
     * @param imageUri file path or URI string
     * @param maxSizeKB Maximum compressed size in KB (default 200KB for Firestore)
     * @return Base64 string of compressed image, prefixed with "data:image/jpeg;base64,"
     */
    suspend fun convertImageToBase64(
        context: Context,
        imageUri: String,
        maxSizeKB: Int = 200
    ): String {
        if (imageUri.isEmpty()) {
            throw IllegalArgumentException("Image URI tidak boleh kosong")
        }

        Log.d(TAG, "Converting image from: $imageUri")

        try {
            // Step 1: Decode bitmap from source
            val originalBitmap = decodeBitmapFromSource(context, imageUri)
                ?: throw IllegalArgumentException("Gagal decode gambar dari: $imageUri")

            Log.d(TAG, "Original size: ${originalBitmap.width}x${originalBitmap.height}")

            // Step 2: Resize to max 600x600 to keep Base64 small
            val resizedBitmap = resizeBitmap(originalBitmap, maxWidth = 600, maxHeight = 600)
            if (resizedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }

            Log.d(TAG, "Resized to: ${resizedBitmap.width}x${resizedBitmap.height}")

            // Step 3: Compress with adaptive quality to fit under maxSizeKB
            val base64String = compressToBase64(resizedBitmap, maxSizeKB)
            resizedBitmap.recycle()

            Log.d(TAG, "Final Base64 length: ${base64String.length} chars (~${base64String.length / 1024} KB)")

            return base64String

        } catch (e: Exception) {
            Log.e(TAG, "Conversion failed: ${e.message}", e)
            throw Exception("Gagal memproses foto: ${e.message ?: "Unknown error"}")
        }
    }

    /**
     * Decode bitmap from various source types:
     * - Plain file path (/data/...)
     * - file:// URI
     * - content:// URI
     */
    private fun decodeBitmapFromSource(context: Context, imageUri: String): Bitmap? {
        Log.d(TAG, "=== DECODE BITMAP START ===")
        Log.d(TAG, "Source URI: $imageUri")
        
        return when {
            // Plain file path
            imageUri.startsWith("/") -> {
                Log.d(TAG, "Reading as file path")
                val file = File(imageUri)
                Log.d(TAG, "File exists: ${file.exists()}")
                Log.d(TAG, "File size: ${file.length()} bytes")
                Log.d(TAG, "File last modified: ${java.util.Date(file.lastModified())}")
                
                if (!file.exists()) {
                    Log.e(TAG, "File does not exist: $imageUri")
                    null
                } else {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    Log.d(TAG, "Bitmap decoded: ${bitmap?.width}x${bitmap?.height}")
                    bitmap
                }
            }
            // file:// URI - extract path and read directly
            imageUri.startsWith("file://") -> {
                Log.d(TAG, "Reading as file:// URI")
                val uri = Uri.parse(imageUri)
                val path = uri.path
                Log.d(TAG, "Extracted path: $path")
                
                if (path == null) {
                    Log.e(TAG, "Cannot extract path from file URI: $imageUri")
                    null
                } else {
                    val file = File(path)
                    Log.d(TAG, "File exists: ${file.exists()}")
                    Log.d(TAG, "File size: ${file.length()} bytes")
                    Log.d(TAG, "File last modified: ${java.util.Date(file.lastModified())}")
                    
                    if (!file.exists()) {
                        Log.e(TAG, "File does not exist: $path")
                        null
                    } else {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        Log.d(TAG, "Bitmap decoded: ${bitmap?.width}x${bitmap?.height}")
                        bitmap
                    }
                }
            }
            // content:// URI - use ContentResolver
            imageUri.startsWith("content://") -> {
                Log.d(TAG, "Reading as content:// URI")
                val uri = Uri.parse(imageUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e(TAG, "Cannot open content URI: $imageUri")
                    null
                } else {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    Log.d(TAG, "Bitmap decoded: ${bitmap?.width}x${bitmap?.height}")
                    bitmap
                }
            }
            else -> {
                // Try as generic URI via ContentResolver
                Log.d(TAG, "Reading as generic URI")
                try {
                    val uri = Uri.parse(imageUri)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = inputStream?.let { BitmapFactory.decodeStream(it) }
                    inputStream?.close()
                    Log.d(TAG, "Bitmap decoded: ${bitmap?.width}x${bitmap?.height}")
                    bitmap
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to read generic URI: ${e.message}")
                    null
                }
            }
        }.also {
            Log.d(TAG, "=== DECODE BITMAP END ===")
        }
    }

    /**
     * Resize bitmap to fit within maxWidth x maxHeight while maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height,
            1.0f // Don't upscale
        )

        if (scale >= 1.0f) {
            return bitmap // Already within limits
        }

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        Log.d(TAG, "Resizing from ${width}x${height} to ${newWidth}x${newHeight}")
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Compress bitmap to Base64 string, adaptively reducing quality until under maxSizeKB
     */
    private fun compressToBase64(bitmap: Bitmap, maxSizeKB: Int): String {
        var quality = 70 // Start at 70% quality
        val minQuality = 20

        while (quality >= minQuality) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            val sizeKB = byteArray.size / 1024

            Log.d(TAG, "Compressed at quality=$quality: ${sizeKB}KB")

            if (sizeKB <= maxSizeKB || quality <= minQuality) {
                // Encode to Base64 without line wraps (cleaner for Firestore)
                val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                Log.d(TAG, "Final compressed size: ${sizeKB}KB at quality=$quality")
                return base64
            }

            quality -= 10 // Reduce quality by 10% each iteration
        }

        // Fallback: just use minimum quality
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, minQuality, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Legacy method - kept for compatibility
     * Now converts to Base64 instead of uploading to Firebase Storage
     */
    suspend fun uploadImage(context: Context, imageUri: String, folder: String = "hewan"): String {
        Log.d(TAG, "uploadImage called - converting to Base64 (folder=$folder)")
        return convertImageToBase64(context, imageUri)
    }

    /**
     * Delete image - no-op for Base64 storage
     */
    suspend fun deleteImage(imageUrl: String) {
        // No-op: Base64 images are stored in Firestore, deleted with document
        Log.d(TAG, "Delete not needed for Base64 storage")
    }
}