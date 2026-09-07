package com.katharina.plants.ui.history

import app.cash.turbine.test
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.local.entity.IdentificationEntity
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
class HistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var dao: IdentificationDao
    private lateinit var viewModel: HistoryViewModel

    private val fakeIdentifications = MutableStateFlow<List<IdentificationEntity>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = mockk(relaxed = true)
        every { dao.getAllIdentifications() } returns fakeIdentifications
        viewModel = HistoryViewModel(dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `identifications flow reflects dao updates`() = runTest {
        val list = listOf(
            IdentificationEntity(1, 1000L, "path", "Species", "Scientific", "Common", 0.9)
        )
        
        viewModel.identifications.test {
            assertEquals(emptyList<IdentificationEntity>(), awaitItem())
            
            fakeIdentifications.value = list
            assertEquals(list, awaitItem())
        }
    }

    @Test
    fun `deleteIdentification calls dao`() = runTest {
        viewModel.deleteIdentification(123L)
        coVerify { dao.deleteIdentification(123L) }
    }
}
