package com.katharina.plants.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "identifications")
data class IdentificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val imagePath: String,
    val speciesName: String,
    val scientificName: String,
    val confidenceScore: Double
)
