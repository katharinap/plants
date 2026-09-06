package com.katharina.plants.data.remote

import com.katharina.plants.data.remote.dto.PlantNetResponseDto
import okhttp3.MultipartBody
import retrofit2.http.*

interface PlantNetApiService {
    @Multipart
    @POST("v2/identify/all")
    suspend fun identify(
        @Query("api-key") apiKey: String,
        @Part images: List<MultipartBody.Part>,
        @Part("organs") organs: List<String>
    ): PlantNetResponseDto
}
