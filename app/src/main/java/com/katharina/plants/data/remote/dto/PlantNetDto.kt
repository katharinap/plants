package com.katharina.plants.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlantNetResponseDto(
    val results: List<PlantNetResultDto>
)

@Serializable
data class PlantNetResultDto(
    val score: Double,
    val species: PlantNetSpeciesDto
)

@Serializable
data class PlantNetSpeciesDto(
    val scientificName: String,
    val family: PlantNetFamilyDto,
    val commonNames: List<String> = emptyList()
)

@Serializable
data class PlantNetFamilyDto(
    val scientificNameWithoutAuthor: String
)
