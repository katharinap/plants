package com.katharina.plants.domain.model

data class PlantIdentificationResult(
    val speciesName: String,
    val scientificName: String,
    val commonNames: List<String>,
    val confidenceScore: Double,
    val family: String,
    val thumbnailUrl: String?
)
