package com.katharina.plants.ui.plantid

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.repository.FakePlantRepository
import com.katharina.plants.data.util.ConnectivityObserver
import com.katharina.plants.data.util.FileStorage
import com.katharina.plants.data.util.ImageOptimizer
import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import com.katharina.plants.domain.model.PlantIdentificationResult
import com.katharina.plants.domain.repository.PlantRepository
import com.katharina.plants.ui.theme.PlantsTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantIdScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val dao: IdentificationDao = mockk(relaxed = true)
    private val connectivityObserver: ConnectivityObserver = mockk(relaxed = true) {
        every { observe() } returns flowOf(ConnectivityObserver.Status.Available)
    }
    private val imageOptimizer: ImageOptimizer = mockk {
        coEvery { optimize(any()) } returns byteArrayOf(0)
    }
    private val fileStorage: FileStorage = mockk(relaxed = true)

    @Test
    fun identifyButton_disabledInitially() {
        val repository = FakePlantRepository()
        val viewModel = PlantIdViewModel(repository, dao, connectivityObserver, imageOptimizer, fileStorage)

        composeTestRule.setContent {
            PlantsTheme {
                PlantIdScreen(
                    viewModel = viewModel,
                    onHistoryClick = {},
                    onAboutClick = {},
                    onSettingsClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Identify").assertIsNotEnabled()
    }

    @Test
    fun identifyButton_showsLoadingThenResults() {
        val repository = FakePlantRepository()
        repository.simulatedDelayMillis = 500L 
        val viewModel = PlantIdViewModel(repository, dao, connectivityObserver, imageOptimizer, fileStorage)

        composeTestRule.setContent {
            PlantsTheme {
                PlantIdScreen(
                    viewModel = viewModel,
                    onHistoryClick = {},
                    onAboutClick = {},
                    onSettingsClick = {}
                )
            }
        }

        // Simulate image selection
        viewModel.onImageSelected(Uri.EMPTY)

        // Click Identify
        composeTestRule.onNodeWithText("Identify").performClick()

        // Should show results eventually
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Monstera deliciosa").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confidence: 98%").assertIsDisplayed()
    }

    @Test
    fun errorState_showsRetryButton() {
        val repository = FakePlantRepository()
        repository.shouldReturnError = true
        val viewModel = PlantIdViewModel(repository, dao, connectivityObserver, imageOptimizer, fileStorage)

        composeTestRule.setContent {
            PlantsTheme {
                PlantIdScreen(
                    viewModel = viewModel,
                    onHistoryClick = {},
                    onAboutClick = {},
                    onSettingsClick = {}
                )
            }
        }

        viewModel.onImageSelected(Uri.EMPTY)
        composeTestRule.onNodeWithText("Identify").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Fake repository error").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Fake repository error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun emptySuccessState_showsNoResultsMessage() {
        val repository = object : PlantRepository {
            override suspend fun identify(
                images: List<ImageInput>,
                organs: List<Organ>
            ): Result<List<PlantIdentificationResult>> {
                return Result.success(emptyList())
            }
        }
        val viewModel = PlantIdViewModel(repository, dao, connectivityObserver, imageOptimizer, fileStorage)

        composeTestRule.setContent {
            PlantsTheme {
                PlantIdScreen(
                    viewModel = viewModel,
                    onHistoryClick = {},
                    onAboutClick = {},
                    onSettingsClick = {}
                )
            }
        }

        viewModel.onImageSelected(Uri.EMPTY)
        composeTestRule.onNodeWithText("Identify").performClick()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("No plants identified. Try another photo.").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("No plants identified. Try another photo.").assertIsDisplayed()
    }
}

// Helper to wait until node is found
private fun ComposeTestRule.onAllNodesWithText(text: String) =
    onAllNodes(hasText(text))
