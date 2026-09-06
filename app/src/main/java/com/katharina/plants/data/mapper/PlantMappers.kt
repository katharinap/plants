package com.katharina.plants.data.mapper

import com.katharina.plants.data.remote.dto.PlantNetResultDto
import com.katharina.plants.domain.model.PlantIdentificationResult

fun PlantNetResultDto.toDomain(): PlantIdentificationResult {
    return PlantIdentificationResult(
        speciesName = species.scientificName,
        scientificName = species.scientificName,
        commonNames = species.commonNames,
        confidenceScore = score,
        family = species.family.scientificNameWithoutAuthor,
        thumbnailUrl = null // Pl@ntNet identifies from photos but results might not have thumbs in basic identify
    )
}
