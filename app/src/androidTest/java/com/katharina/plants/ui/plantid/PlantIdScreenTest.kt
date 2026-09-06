package com.katharina.plants.ui.plantid

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.katharina.plants.data.repository.FakePlantRepository
import com.katharina.plants.ui.theme.PlantsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantIdScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun identifyButton_showsLoadingThenResults() {
        val repository = FakePlantRepository()
        // Make the delay longer so we can reliably catch the loading state if needed,
        // though UnconfinedTestDispatcher usually skips it.
        // For Compose tests, it's real time unless we use special dispatchers.
        repository.simulatedDelayMillis = 1000L 
        val viewModel = PlantIdViewModel(repository)

        composeTestRule.setContent {
            PlantsTheme {
                PlantIdScreen(viewModel = viewModel)
            }
        }

        // Initial state
        composeTestRule.onNodeWithText("Select an image to identify").assertIsDisplayed()

        // Click Identify
        composeTestRule.onNodeWithText("Identify").performClick()

        // Should show results eventually (FakePlantRepository returns Monstera)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Monstera deliciosa").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confidence: 98%").assertIsDisplayed()
    }
}

// Helper to wait until node is found
private fun ComposeTestRule.onAllNodesWithText(text: String) =
    onAllNodes(hasText(text))
