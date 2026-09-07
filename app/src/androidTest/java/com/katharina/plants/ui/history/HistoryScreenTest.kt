package com.katharina.plants.ui.history

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.katharina.plants.data.local.entity.IdentificationEntity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun historyItems_areDisplayed() {
        val list = listOf(
            IdentificationEntity(
                id = 1,
                timestamp = System.currentTimeMillis(),
                imagePath = "",
                speciesName = "Monstera deliciosa",
                scientificName = "Monstera deliciosa Liebm.",
                commonNames = "Swiss cheese plant",
                confidenceScore = 0.98
            )
        )

        composeTestRule.setContent {
            HistoryContent(
                identifications = list,
                onDeleteClick = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Swiss cheese plant").assertIsDisplayed()
    }
}
