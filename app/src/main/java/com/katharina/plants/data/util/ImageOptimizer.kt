package com.katharina.plants.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ImageOptimizer @Inject constructor(
    private val context: Context
) {
    suspend fun optimize(uri: Uri, maxWidth: Int = 1080, maxHeight: Int = 1080): ByteArray = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri) 
            ?: throw Exception("Failed to open input stream")
        
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        var inSampleSize = 1
        if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
            val halfHeight: Int = options.outHeight / 2
            val halfWidth: Int = options.outWidth / 2
            while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                inSampleSize *= 2
            }
        }

        val finalOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        
        val finalInputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Failed to open input stream")
        val bitmap = BitmapFactory.decodeStream(finalInputStream, null, finalOptions)
            ?: throw Exception("Failed to decode bitmap")
        finalInputStream.close()

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.toByteArray()
    }
}
