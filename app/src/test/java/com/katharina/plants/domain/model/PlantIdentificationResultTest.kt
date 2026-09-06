package com.katharina.plants.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlantIdentificationResultTest {

    @Test
    fun `PlantIdentificationResult preserves properties`() {
        val result = PlantIdentificationResult(
            speciesName = "Monstera deliciosa",
            scientificName = "Monstera deliciosa Liebm.",
            commonNames = listOf("Swiss cheese plant", "Cereiman"),
            confidenceScore = 0.95,
            family = "Araceae",
            thumbnailUrl = "https://example.com/image.jpg"
        )

        assertEquals("Monstera deliciosa", result.speciesName)
        assertEquals("Monstera deliciosa Liebm.", result.scientificName)
        assertEquals(listOf("Swiss cheese plant", "Cereiman"), result.commonNames)
        assertEquals(0.95, result.confidenceScore, 0.001)
        assertEquals("Araceae", result.family)
        assertEquals("https://example.com/image.jpg", result.thumbnailUrl)
    }

    @Test
    fun `PlantIdentificationResult equality works`() {
        val result1 = PlantIdentificationResult("A", "B", listOf("C"), 0.5, "D", null)
        val result2 = PlantIdentificationResult("A", "B", listOf("C"), 0.5, "D", null)
        
        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }
}
