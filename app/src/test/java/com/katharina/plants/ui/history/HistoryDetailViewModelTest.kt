package com.katharina.plants.ui.history

import androidx.lifecycle.SavedStateHandle
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.local.entity.IdentificationEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: IdentificationDao
    private lateinit var viewModel: HistoryDetailViewModel

    private val sampleEntity = IdentificationEntity(
        id = 123L,
        timestamp = 1000L,
        imagePath = "path",
        speciesName = "Monstera",
        scientificName = "Monstera deliciosa",
        commonNames = "Swiss cheese",
        confidenceScore = 0.9
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = mockk()
        coEvery { dao.getIdentificationById(123L) } returns sampleEntity
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel loads identification on init`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("identificationId" to 123L))
        viewModel = HistoryDetailViewModel(dao, savedStateHandle)
        
        advanceUntilIdle()
        
        assertEquals(sampleEntity, viewModel.identification.value)
    }

    @Test
    fun `viewModel handles null identification`() = runTest {
        coEvery { dao.getIdentificationById(any()) } returns null
        val savedStateHandle = SavedStateHandle(mapOf("identificationId" to 999L))
        viewModel = HistoryDetailViewModel(dao, savedStateHandle)
        
        advanceUntilIdle()
        
        assertEquals(null, viewModel.identification.value)
    }
}
