package com.katharina.plants.ui.plantid

import android.net.Uri
import app.cash.turbine.test
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.repository.FakePlantRepository
import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var viewModel: PlantIdViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
        
        repository = FakePlantRepository().apply {
            simulatedDelayMillis = 100 // Add a small delay to test Loading state
        }
        dao = mockk(relaxed = true)
        viewModel = PlantIdViewModel(repository, dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onOrganSelected updates selectedOrgan`() = runTest {
        viewModel.onOrganSelected(Organ.LEAF)
        assertEquals(Organ.LEAF, viewModel.selectedOrgan.value)
    }

    @Test
    fun `identifyPlants saves result to dao on success`() = runTest {
        viewModel.onImageSelected(Uri.parse("fake"))
        
        viewModel.identifyPlants()
        advanceUntilIdle()

        coVerify { dao.insertIdentification(any()) }
    }

    @Test
    fun `identifyPlants does not save to dao on failure`() = runTest {
        viewModel.onImageSelected(Uri.parse("fake"))
        repository.shouldReturnError = true

        viewModel.identifyPlants()
        advanceUntilIdle()

        coVerify(exactly = 0) { dao.insertIdentification(any()) }
    }

    @Test
    fun `initial state is Idle`() =
        runTest {
            assertEquals(PlantIdUiState.Idle, viewModel.uiState.value)
            assertEquals(null, viewModel.selectedUri.value)
        }

    @Test
    fun `onImageSelected updates selectedUri and resets uiState`() = runTest {
        val uri = Uri.parse("fake")
        viewModel.onImageSelected(uri)
        
        assertEquals(uri, viewModel.selectedUri.value)
        assertEquals(PlantIdUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `identifyPlants transitions Idle to Loading to Success`() =
        runTest {
            viewModel.onImageSelected(Uri.parse("fake"))
            
            viewModel.uiState.test {
                assertEquals(PlantIdUiState.Idle, awaitItem())

                viewModel.identifyPlants()

                assertEquals(PlantIdUiState.Loading, awaitItem())
                
                advanceUntilIdle()

                val successState = awaitItem()
                assertTrue(successState is PlantIdUiState.Success)
                assertEquals(2, (successState as PlantIdUiState.Success).results.size)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `identifyPlants transitions to Error on failure`() =
        runTest {
            viewModel.onImageSelected(Uri.parse("fake"))
            repository.shouldReturnError = true

            viewModel.uiState.test {
                assertEquals(PlantIdUiState.Idle, awaitItem())

                viewModel.identifyPlants()

                assertEquals(PlantIdUiState.Loading, awaitItem())
                
                advanceUntilIdle()

                val errorState = awaitItem()
                assertTrue(errorState is PlantIdUiState.Error)
                assertEquals("Fake repository error", (errorState as PlantIdUiState.Error).message)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `identifyPlants passes selected organ to repository`() = runTest {
        val uri = Uri.parse("fake")
        viewModel.onImageSelected(uri)
        viewModel.onOrganSelected(Organ.FRUIT)

        viewModel.identifyPlants()
        advanceUntilIdle()

        assertEquals(listOf(Organ.FRUIT), repository.lastCapturedOrgans)
    }
}
