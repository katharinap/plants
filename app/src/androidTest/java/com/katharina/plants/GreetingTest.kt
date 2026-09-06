package com.katharina.plants

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.katharina.plants.ui.theme.PlantsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GreetingTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greeting_displaysCorrectText() {
        composeTestRule.setContent {
            PlantsTheme {
                Greeting(name = "Test")
            }
        }

        composeTestRule.onNodeWithText("Hello Test!").assertExists()
    }
}
