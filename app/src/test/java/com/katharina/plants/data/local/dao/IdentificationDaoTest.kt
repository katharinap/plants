package com.katharina.plants.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.katharina.plants.data.local.PlantsDatabase
import com.katharina.plants.data.local.entity.IdentificationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IdentificationDaoTest {

    private lateinit var database: PlantsDatabase
    private lateinit var dao: IdentificationDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            PlantsDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.dao
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertIdentification and getAllIdentifications work correctly`() = runTest {
        val entity = IdentificationEntity(
            timestamp = 123456789L,
            imagePath = "/path/to/image.jpg",
            speciesName = "Monstera deliciosa",
            scientificName = "Monstera deliciosa Liebm.",
            commonNames = "Swiss cheese plant",
            confidenceScore = 0.98
        )

        dao.insertIdentification(entity)
        val all = dao.getAllIdentifications().first()

        assertEquals(1, all.size)
        assertEquals("Monstera deliciosa", all[0].speciesName)
        assertEquals("Swiss cheese plant", all[0].commonNames)
        assertEquals(123456789L, all[0].timestamp)
    }

    @Test
    fun `deleteIdentification works correctly`() = runTest {
        val entity = IdentificationEntity(
            id = 1,
            timestamp = 123456789L,
            imagePath = "/path/to/image.jpg",
            speciesName = "Monstera deliciosa",
            scientificName = "Monstera deliciosa Liebm.",
            commonNames = "Swiss cheese plant",
            confidenceScore = 0.98
        )

        dao.insertIdentification(entity)
        dao.deleteIdentification(1)
        
        val all = dao.getAllIdentifications().first()
        assertEquals(0, all.size)
    }

    @Test
    fun `getIdentificationById returns correct record`() = runTest {
        val entity = IdentificationEntity(
            id = 10,
            timestamp = 123456789L,
            imagePath = "/path/to/image.jpg",
            speciesName = "Monstera deliciosa",
            scientificName = "Monstera deliciosa Liebm.",
            commonNames = "Swiss cheese plant",
            confidenceScore = 0.98
        )

        dao.insertIdentification(entity)
        
        val found = dao.getIdentificationById(10)
        assertEquals(entity, found)
    }
}
