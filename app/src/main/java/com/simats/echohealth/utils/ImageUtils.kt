package com.simats.echohealth.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

object ImageUtils {
    
    /**
     * Validate file before processing (supports images and PDFs)
     */
    fun validateFile(context: Context, fileUri: Uri): String? {
        return try {
            Log.d("ImageUtils", "🔍 Validating file: $fileUri")
            
            val mimeType = context.contentResolver.getType(fileUri)
            Log.d("ImageUtils", "📋 MIME type: $mimeType")
            
            // Check if it's a supported file type
            val supportedTypes = listOf(
                "image/jpeg", 
                "image/jpg", 
                "image/png", 
                "image/webp",
                "application/pdf"
            )
            if (mimeType !in supportedTypes) {
                Log.e("ImageUtils", "❌ Unsupported MIME type: $mimeType")
                return "Invalid file format. Please upload JPG, PNG, or PDF."
            }
            
            val inputStream = context.contentResolver.openInputStream(fileUri)
            if (inputStream == null) {
                Log.e("ImageUtils", "❌ Cannot open input stream")
                return "Cannot access file. Please try selecting a different file."
            }
            
            val fileSize = inputStream.available()
            Log.d("ImageUtils", "📁 File size: $fileSize bytes (${fileSize / 1024} KB)")
            
            if (fileSize > 10 * 1024 * 1024) {
                Log.e("ImageUtils", "❌ File too large: ${fileSize / 1024 / 1024} MB")
                inputStream.close()
                return "File is too large (${fileSize / 1024 / 1024} MB). Please select a file under 10MB."
            }
            
            if (fileSize < 1024) {
                Log.e("ImageUtils", "❌ File too small: $fileSize bytes")
                inputStream.close()
                return "File appears to be corrupted or too small. Please select a different file."
            }
            
            // For PDF files, just check file size and MIME type
            if (mimeType == "application/pdf") {
                inputStream.close()
                Log.d("ImageUtils", "✅ PDF validation passed")
                return null // No error
            }
            
            // For image files, check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                Log.e("ImageUtils", "❌ Invalid image dimensions: ${options.outWidth}x${options.outHeight}")
                return "Invalid image file. Please select a valid image."
            }
            
            if (options.outWidth < 50 || options.outHeight < 50) {
                Log.e("ImageUtils", "❌ Image too small: ${options.outWidth}x${options.outHeight}")
                return "Image is too small (${options.outWidth}x${options.outHeight}). Please select an image at least 50x50 pixels."
            }
            
            if (options.outWidth > 4000 || options.outHeight > 4000) {
                Log.e("ImageUtils", "❌ Image too large: ${options.outWidth}x${options.outHeight}")
                return "Image is too large (${options.outWidth}x${options.outHeight}). Please select an image under 4000x4000 pixels."
            }
            
            Log.d("ImageUtils", "✅ File validation passed: ${options.outWidth}x${options.outHeight}, ${options.outMimeType}")
            null // No error
        } catch (e: Exception) {
            Log.e("ImageUtils", "❌ Error validating image: ${e.message}")
            "Error validating image: ${e.message}"
        }
    }
    
    /**
     * Convert file to Base64 with proper MIME type prefix
     */
    fun convertFileToBase64(context: Context, fileUri: Uri): String? {
        return try {
            Log.d("ImageUtils", "=== Starting file conversion for URI: $fileUri ===")
            
            // Get MIME type for debugging
            val mimeType = context.contentResolver.getType(fileUri)
            Log.d("ImageUtils", "📋 MIME type: $mimeType")
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
            if (inputStream == null) {
                Log.e("ImageUtils", "❌ Failed to open input stream for URI: $fileUri")
                return null
            }
            
            // Check file size first
            val fileSize = inputStream.available()
            Log.d("ImageUtils", "📁 File size: $fileSize bytes (${fileSize / 1024} KB)")
            
            if (fileSize > 10 * 1024 * 1024) { // 10MB limit
                Log.e("ImageUtils", "❌ File too large: $fileSize bytes (${fileSize / 1024 / 1024} MB)")
                inputStream.close()
                return null
            }
            
            if (fileSize < 1024) { // Less than 1KB is suspicious
                Log.e("ImageUtils", "❌ File too small: $fileSize bytes - likely corrupted")
                inputStream.close()
                return null
            }
            
            // Read file content
            val fileBytes = inputStream.readBytes()
            inputStream.close()
            
            if (fileBytes.isEmpty()) {
                Log.e("ImageUtils", "❌ File is empty")
                return null
            }
            
            // Convert to Base64
            val base64String = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
            Log.d("ImageUtils", "📄 Base64 string length: ${base64String.length}")
            
            // Return raw Base64 string (API expects this format)
            Log.d("ImageUtils", "✅ File conversion successful - Raw Base64 length: ${base64String.length}")
            Log.d("ImageUtils", "📋 MIME type: $mimeType")
            Log.d("ImageUtils", "Note: API expects raw Base64, not data URL")
            
            base64String
        } catch (e: Exception) {
            Log.e("ImageUtils", "❌ Error converting file to base64: ${e.message}")
            Log.e("ImageUtils", "❌ Exception type: ${e.javaClass.simpleName}")
            Log.e("ImageUtils", "❌ Stack trace: ${e.stackTraceToString()}")
            null
        }
    }
    
    fun convertImageToBase64(context: Context, imageUri: Uri): String? {
        return try {
            Log.d("ImageUtils", "=== Starting image conversion for URI: $imageUri ===")
            
            // Get MIME type for debugging
            val mimeType = context.contentResolver.getType(imageUri)
            Log.d("ImageUtils", "MIME type: $mimeType")
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("ImageUtils", "❌ Failed to open input stream for URI: $imageUri")
                return null
            }
            
            // Check file size first
            val fileSize = inputStream.available()
            Log.d("ImageUtils", "📁 File size: $fileSize bytes (${fileSize / 1024} KB)")
            
            if (fileSize > 10 * 1024 * 1024) { // 10MB limit
                Log.e("ImageUtils", "❌ Image too large: $fileSize bytes (${fileSize / 1024 / 1024} MB)")
                inputStream.close()
                return null
            }
            
            if (fileSize < 1024) { // Less than 1KB is suspicious
                Log.e("ImageUtils", "❌ Image too small: $fileSize bytes - likely corrupted")
                inputStream.close()
                return null
            }
            
            // Try to decode the bitmap with options for better debugging
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            // First pass - just get dimensions
            BitmapFactory.decodeStream(inputStream, null, options)
            Log.d("ImageUtils", "📐 Image dimensions: ${options.outWidth}x${options.outHeight}")
            Log.d("ImageUtils", "🎨 Image format: ${options.outMimeType}")
            
            // Reset stream for actual decoding
            inputStream.close()
            val newInputStream = context.contentResolver.openInputStream(imageUri)
            
            // Second pass - decode the actual bitmap
            val bitmap = BitmapFactory.decodeStream(newInputStream)
            newInputStream?.close()
            
            if (bitmap != null) {
                Log.d("ImageUtils", "✅ Successfully decoded bitmap: ${bitmap.width}x${bitmap.height}")
                
                // Validate image dimensions
                if (bitmap.width < 50 || bitmap.height < 50) {
                    Log.e("ImageUtils", "❌ Image too small: ${bitmap.width}x${bitmap.height} (minimum 50x50)")
                    return null
                }
                
                if (bitmap.width > 4000 || bitmap.height > 4000) {
                    Log.e("ImageUtils", "❌ Image too large: ${bitmap.width}x${bitmap.height} (maximum 4000x4000)")
                    return null
                }
                
                // Check if bitmap is valid (not corrupted)
                if (bitmap.isRecycled) {
                    Log.e("ImageUtils", "❌ Bitmap is recycled - invalid image")
                    return null
                }
                
                // Check bitmap configuration
                Log.d("ImageUtils", "🔧 Bitmap config: ${bitmap.config}")
                Log.d("ImageUtils", "🎯 Bitmap density: ${bitmap.density}")
                
                val result = convertBitmapToBase64(bitmap)
                if (result != null) {
                    Log.d("ImageUtils", "✅ Image conversion successful - Base64 length: ${result.length}")
                } else {
                    Log.e("ImageUtils", "❌ Failed to convert bitmap to base64")
                }
                result
            } else {
                Log.e("ImageUtils", "❌ Failed to decode bitmap from URI: $imageUri")
                Log.e("ImageUtils", "❌ This usually means the image format is not supported or corrupted")
                Log.e("ImageUtils", "❌ MIME type was: $mimeType")
                Log.e("ImageUtils", "❌ Detected format: ${options.outMimeType}")
                null
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "❌ Error converting image to base64: ${e.message}")
            Log.e("ImageUtils", "❌ Exception type: ${e.javaClass.simpleName}")
            Log.e("ImageUtils", "❌ Stack trace: ${e.stackTraceToString()}")
            null
        }
    }
    
    fun convertBitmapToBase64(bitmap: Bitmap): String? {
        return try {
            Log.d("ImageUtils", "Converting bitmap to base64: ${bitmap.width}x${bitmap.height}")
            
            val outputStream = ByteArrayOutputStream()
            
            // Try different compression levels to find the best balance
            var quality = 85
            var compressedSize = 0
            
            // First attempt with 85% quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedSize = outputStream.size()
            Log.d("ImageUtils", "Compressed size with $quality% quality: $compressedSize bytes")
            
            // If still too large, reduce quality
            if (compressedSize > 5 * 1024 * 1024) { // 5MB
                outputStream.reset()
                quality = 70
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedSize = outputStream.size()
                Log.d("ImageUtils", "Reduced quality to $quality%: $compressedSize bytes")
            }
            
            // If still too large, reduce further
            if (compressedSize > 8 * 1024 * 1024) { // 8MB
                outputStream.reset()
                quality = 60
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedSize = outputStream.size()
                Log.d("ImageUtils", "Reduced quality to $quality%: $compressedSize bytes")
            }
            
            val byteArray = outputStream.toByteArray()
            outputStream.close()
            
            if (byteArray.isEmpty()) {
                Log.e("ImageUtils", "Compressed byte array is empty")
                return null
            }
            
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            Log.d("ImageUtils", "Base64 string length: ${base64String.length}")
            Log.d("ImageUtils", "Base64 preview: ${base64String.take(50)}...")
            Log.d("ImageUtils", "Final compression quality: $quality%")
            
            // Return raw Base64 string (API expects this format)
            Log.d("ImageUtils", "Raw Base64 string length: ${base64String.length}")
            Log.d("ImageUtils", "Note: API expects raw Base64, not data URL")
            
            base64String
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error converting bitmap to base64: ${e.message}")
            Log.e("ImageUtils", "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert file from path to Base64 with proper MIME type prefix
     */
    fun convertFilePathToBase64(filePath: String): String? {
        return try {
            Log.d("ImageUtils", "=== Starting file conversion for path: $filePath ===")
            
            val file = File(filePath)
            if (!file.exists()) {
                Log.e("ImageUtils", "❌ File does not exist: $filePath")
                return null
            }
            
            val fileSize = file.length()
            Log.d("ImageUtils", "📁 File size: $fileSize bytes (${fileSize / 1024} KB)")
            
            if (fileSize > 10 * 1024 * 1024) { // 10MB limit
                Log.e("ImageUtils", "❌ File too large: $fileSize bytes (${fileSize / 1024 / 1024} MB)")
                return null
            }
            
            if (fileSize < 1024) { // Less than 1KB is suspicious
                Log.e("ImageUtils", "❌ File too small: $fileSize bytes - likely corrupted")
                return null
            }
            
            // Read file content
            val fileBytes = file.readBytes()
            if (fileBytes.isEmpty()) {
                Log.e("ImageUtils", "❌ File is empty")
                return null
            }
            
            // Convert to Base64
            val base64String = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
            Log.d("ImageUtils", "📄 Base64 string length: ${base64String.length}")
            
            // Validate file extension
            val fileExtension = file.extension.lowercase()
            val supportedExtensions = listOf("jpg", "jpeg", "png", "webp", "pdf")
            if (fileExtension !in supportedExtensions) {
                Log.e("ImageUtils", "❌ Unsupported file extension: $fileExtension")
                return null
            }
            
            // Return raw Base64 string (API expects this format)
            Log.d("ImageUtils", "✅ File conversion successful - Raw Base64 length: ${base64String.length}")
            Log.d("ImageUtils", "📋 File extension: $fileExtension")
            Log.d("ImageUtils", "Note: API expects raw Base64, not data URL")
            
            base64String
        } catch (e: Exception) {
            Log.e("ImageUtils", "❌ Error converting file to base64: ${e.message}")
            Log.e("ImageUtils", "❌ Exception type: ${e.javaClass.simpleName}")
            Log.e("ImageUtils", "❌ Stack trace: ${e.stackTraceToString()}")
            null
        }
    }
    
    fun convertImagePathToBase64(imagePath: String): String? {
        return try {
            Log.d("ImageUtils", "Starting image conversion for path: $imagePath")
            
            val file = java.io.File(imagePath)
            if (!file.exists()) {
                Log.e("ImageUtils", "File does not exist: $imagePath")
                return null
            }
            
            val fileSize = file.length()
            Log.d("ImageUtils", "File size: $fileSize bytes (${fileSize / 1024} KB)")
            
            if (fileSize > 10 * 1024 * 1024) { // 10MB limit
                Log.e("ImageUtils", "File too large: $fileSize bytes (${fileSize / 1024 / 1024} MB)")
                return null
            }
            
            if (fileSize < 1024) { // Less than 1KB is suspicious
                Log.e("ImageUtils", "File too small: $fileSize bytes - likely corrupted")
                return null
            }
            
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap != null) {
                Log.d("ImageUtils", "Successfully decoded bitmap from path: ${bitmap.width}x${bitmap.height}")
                
                // Validate image dimensions
                if (bitmap.width < 50 || bitmap.height < 50) {
                    Log.e("ImageUtils", "Image too small: ${bitmap.width}x${bitmap.height} (minimum 50x50)")
                    return null
                }
                
                if (bitmap.width > 4000 || bitmap.height > 4000) {
                    Log.e("ImageUtils", "Image too large: ${bitmap.width}x${bitmap.height} (maximum 4000x4000)")
                    return null
                }
                
                // Check if bitmap is valid (not corrupted)
                if (bitmap.isRecycled) {
                    Log.e("ImageUtils", "Bitmap is recycled - invalid image")
                    return null
                }
                
                val result = convertBitmapToBase64(bitmap)
                if (result != null) {
                    Log.d("ImageUtils", "Image path conversion successful")
                } else {
                    Log.e("ImageUtils", "Failed to convert bitmap to base64")
                }
                result
            } else {
                Log.e("ImageUtils", "Failed to decode bitmap from path: $imagePath")
                Log.e("ImageUtils", "This usually means the image format is not supported or corrupted")
                null
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error converting image path to base64: ${e.message}")
            Log.e("ImageUtils", "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            null
        }
    }
}
