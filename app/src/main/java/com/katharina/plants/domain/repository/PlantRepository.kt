package com.katharina.plants.domain.repository

import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import com.katharina.plants.domain.model.PlantIdentificationResult

interface PlantRepository {
    suspend fun identify(
        images: List<ImageInput>,
        organs: List<Organ>
    ): Result<List<PlantIdentificationResult>>
}
