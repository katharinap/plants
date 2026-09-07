package com.katharina.plants.data.repository

import com.katharina.plants.BuildConfig
import com.katharina.plants.data.mapper.toDomain
import com.katharina.plants.data.remote.PlantNetApiService
import com.katharina.plants.data.util.ImageOptimizer
import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import com.katharina.plants.domain.model.PlantIdentificationResult
import com.katharina.plants.domain.repository.PlantRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject

class PlantNetRepository @Inject constructor(
    private val api: PlantNetApiService,
    private val imageOptimizer: ImageOptimizer
) : PlantRepository {

    override suspend fun identify(
        images: List<ImageInput>,
        organs: List<Organ>
    ): Result<List<PlantIdentificationResult>> = try {
        val apiKey = BuildConfig.PLANTNET_API_KEY
        
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
        
        val response = api.identify(apiKey, imageParts, organParts)
        Result.success(response.results.map { it.toDomain() })
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        Result.failure(Exception("API Error 400: $errorBody", e))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
