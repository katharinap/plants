package com.katharina.plants.ui.settings

import app.cash.turbine.test
import com.katharina.plants.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel

    private val languageFlow = MutableStateFlow("en")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.languageCode } returns languageFlow
        viewModel = SettingsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `languageCode flow reflects repository updates`() = runTest {
        viewModel.languageCode.test {
            assertEquals("en", awaitItem())
            
            languageFlow.value = "de"
            assertEquals("de", awaitItem())
        }
    }

    @Test
    fun `setLanguageCode calls repository`() = runTest {
        viewModel.setLanguageCode("fr")
        coVerify { repository.setLanguageCode("fr") }
    }
}
