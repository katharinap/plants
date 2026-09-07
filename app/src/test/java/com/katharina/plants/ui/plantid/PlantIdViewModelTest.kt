package com.katharina.plants.ui.plantid

import android.net.Uri
import app.cash.turbine.test
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.repository.FakePlantRepository
import com.katharina.plants.data.util.ConnectivityObserver
import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlantIdViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePlantRepository
    private lateinit var dao: IdentificationDao
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var viewModel: PlantIdViewModel
    
    private val connectivityFlow = MutableStateFlow(ConnectivityObserver.Status.Available)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
        
        repository = FakePlantRepository().apply {
            simulatedDelayMillis = 100
        }
        dao = mockk(relaxed = true)
        connectivityObserver = mockk(relaxed = true)
        every { connectivityObserver.observe() } returns connectivityFlow
        
        viewModel = PlantIdViewModel(repository, dao, connectivityObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `identifyPlants transitions to Offline when network is unavailable`() = runTest {
        connectivityFlow.value = ConnectivityObserver.Status.Unavailable
        advanceUntilIdle()
        
        viewModel.onImageSelected(Uri.parse("fake"))
        viewModel.identifyPlants()
        
        assertEquals(PlantIdUiState.Offline, viewModel.uiState.value)
    }

    @Test
    fun `identifyPlants saves result to dao on success`() = runTest {
        viewModel.onImageSelected(Uri.parse("fake"))
        advanceUntilIdle()
        
        viewModel.identifyPlants()
        advanceUntilIdle()

        coVerify { dao.insertIdentification(any()) }
    }

    @Test
    fun `identifyPlants transitions Idle to Loading to Success`() = runTest {
        viewModel.onImageSelected(Uri.parse("fake"))
        advanceUntilIdle()
        
        viewModel.uiState.test {
            assertEquals(PlantIdUiState.Idle, awaitItem())

            viewModel.identifyPlants()

            assertEquals(PlantIdUiState.Loading, awaitItem())
            
            advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is PlantIdUiState.Success)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `identifyPlants transitions to Error on failure`() = runTest {
        viewModel.onImageSelected(Uri.parse("fake"))
        repository.shouldReturnError = true
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(PlantIdUiState.Idle, awaitItem())

            viewModel.identifyPlants()

            assertEquals(PlantIdUiState.Loading, awaitItem())
            
            advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is PlantIdUiState.Error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `identifyPlants passes selected organ to repository`() = runTest {
        viewModel.onImageSelected(Uri.parse("fake"))
        viewModel.onOrganSelected(Organ.FRUIT)
        advanceUntilIdle()

        viewModel.identifyPlants()
        advanceUntilIdle()

        assertEquals(listOf(Organ.FRUIT), repository.lastCapturedOrgans)
    }
}
