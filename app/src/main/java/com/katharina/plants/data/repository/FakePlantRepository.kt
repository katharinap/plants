package com.katharina.plants.data.repository

import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import com.katharina.plants.domain.model.PlantIdentificationResult
import com.katharina.plants.domain.repository.PlantRepository
import kotlinx.coroutines.delay

class FakePlantRepository : PlantRepository {

    var shouldReturnError = false
    var simulatedDelayMillis = 500L
    
    // Inspection properties for testing
    var lastCapturedImages: List<ImageInput>? = null
    var lastCapturedOrgans: List<Organ>? = null

    override suspend fun identify(
        images: List<ImageInput>,
        organs: List<Organ>
    ): Result<List<PlantIdentificationResult>> {
        lastCapturedImages = images
        lastCapturedOrgans = organs
        
        delay(simulatedDelayMillis)

        if (shouldReturnError) {
            return Result.failure(Exception("Fake repository error"))
        }

        return Result.success(
            listOf(
                PlantIdentificationResult(
                    speciesName = "Monstera deliciosa",
                    scientificName = "Monstera deliciosa Liebm.",
                    commonNames = listOf("Swiss cheese plant"),
                    confidenceScore = 0.98,
                    family = "Araceae",
                    thumbnailUrl = null
                ),
                PlantIdentificationResult(
                    speciesName = "Epipremnum aureum",
                    scientificName = "Epipremnum aureum (Linden & André) G.S.Bunting",
                    commonNames = listOf("Golden pothos", "Devil's ivy"),
                    confidenceScore = 0.15,
                    family = "Araceae",
                    thumbnailUrl = null
                )
            )
        )
    }
}
