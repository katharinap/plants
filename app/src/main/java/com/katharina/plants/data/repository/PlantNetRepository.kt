package com.katharina.plants.data.repository

import com.katharina.plants.BuildConfig
import com.katharina.plants.data.mapper.toDomain
import com.katharina.plants.data.remote.PlantNetApiService
import com.katharina.plants.data.util.ImageOptimizer
import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import com.katharina.plants.domain.model.PlantIdentificationResult
import com.katharina.plants.domain.repository.PlantRepository
import com.katharina.plants.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject

class PlantNetRepository @Inject constructor(
    private val api: PlantNetApiService,
    private val imageOptimizer: ImageOptimizer,
    private val settingsRepository: SettingsRepository
) : PlantRepository {

    override suspend fun identify(
        images: List<ImageInput>,
        organs: List<Organ>
    ): Result<List<PlantIdentificationResult>> = try {
        val apiKey = BuildConfig.PLANTNET_API_KEY
        val lang = settingsRepository.languageCode.first()
        
        val imageParts = images.mapIndexed { index, imageInput ->
            val optimizedImage = imageOptimizer.optimize(imageInput.uri)
            val requestBody = optimizedImage.toRequestBody("image/jpeg".toMediaType())
            // Pl@ntNet expects 'images' part name
            MultipartBody.Part.createFormData("images", "image$index.jpg", requestBody)
        }
        
        val organStrings = if (organs.isEmpty()) {
            images.map { "flower" }
        } else {
            organs.map { it.name.lowercase() }
        }

        val organParts = organStrings.map { 
            MultipartBody.Part.createFormData("organs", it) 
        }
        
        val response = api.identify(apiKey, lang, imageParts, organParts)
        Result.success(response.results.map { it.toDomain() })
    } catch (e: HttpException) {
        val message = when (e.code()) {
            429 -> "Daily API quota exceeded. Please try again tomorrow."
            400 -> {
                val errorBody = e.response()?.errorBody()?.string()
                "Invalid request: $errorBody"
            }
            else -> e.message()
        }
        Result.failure(Exception(message, e))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
