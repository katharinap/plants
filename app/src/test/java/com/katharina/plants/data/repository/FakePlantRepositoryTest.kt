package com.katharina.plants.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakePlantRepositoryTest {

    private val repository = FakePlantRepository()

    @Test
    fun `identify returns success with canned data`() = runTest {
        val result = repository.identify(emptyList(), emptyList())

        assertTrue(result.isSuccess)
        val plants = result.getOrThrow()
        assertEquals(2, plants.size)
        assertEquals("Monstera deliciosa", plants[0].speciesName)
    }

    @Test
    fun `identify returns failure when shouldReturnError is true`() = runTest {
        repository.shouldReturnError = true
        
        val result = repository.identify(emptyList(), emptyList())

        assertTrue(result.isFailure)
        assertEquals("Fake repository error", result.exceptionOrNull()?.message)
    }
}
