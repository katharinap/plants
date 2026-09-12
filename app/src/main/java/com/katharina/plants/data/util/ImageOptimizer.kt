package com.katharina.plants.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

class ImageOptimizer @Inject constructor(
    private val context: Context
) {
    suspend fun optimize(uri: Uri, maxWidth: Int = 1080, maxHeight: Int = 1080): ByteArray = withContext(Dispatchers.IO) {
        val rotation = getRotationDegrees(uri)

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
        val decodedBitmap = BitmapFactory.decodeStream(finalInputStream, null, finalOptions)
            ?: throw Exception("Failed to decode bitmap")
        finalInputStream.close()

        val rotatedBitmap = if (rotation != 0) {
            rotateBitmap(decodedBitmap, rotation)
        } else {
            decodedBitmap
        }

        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        
        if (rotatedBitmap != decodedBitmap) {
            rotatedBitmap.recycle()
        }
        decodedBitmap.recycle()
        
        outputStream.toByteArray()
    }

    private fun getRotationDegrees(uri: Uri): Int {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return 0
        return try {
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        } finally {
            inputStream.close()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
