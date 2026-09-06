package com.katharina.plants.ui.plantid

import android.net.Uri
import app.cash.turbine.test
import com.katharina.plants.data.repository.FakePlantRepository
import com.katharina.plants.domain.model.ImageInput
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlantIdViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakePlantRepository
    private lateinit var viewModel: PlantIdViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
        repository = FakePlantRepository()
        viewModel = PlantIdViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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

                val errorState = awaitItem()
                assertTrue(errorState is PlantIdUiState.Error)
                assertEquals("Fake repository error", (errorState as PlantIdUiState.Error).message)

                cancelAndIgnoreRemainingEvents()
            }
        }
}
