package com.katharina.plants.data.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class FileStorage @Inject constructor(
    private val context: Context
) {
    suspend fun saveImage(bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, "plant_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(directory, fileName)
        file.writeBytes(bytes)
        
        file.absolutePath
    }
}
