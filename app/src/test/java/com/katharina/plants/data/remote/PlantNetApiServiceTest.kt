package com.katharina.plants.data.remote

import com.katharina.plants.data.remote.dto.PlantNetResponseDto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class PlantNetApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: PlantNetApiService

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PlantNetApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `identify parses success response correctly`() = runTest {
        val responseBody = """
            {
                "results": [
                    {
                        "score": 0.98,
                        "species": {
                            "scientificName": "Monstera deliciosa Liebm.",
                            "family": {
                                "scientificNameWithoutAuthor": "Araceae"
                            },
                            "commonNames": ["Swiss cheese plant"]
                        }
                    }
                ]
            }
        """.trimIndent()

        server.enqueue(MockResponse().setBody(responseBody))

        val imagePart = MultipartBody.Part.createFormData(
            "images",
            "test.jpg",
            "dummy".toRequestBody("image/jpeg".toMediaType())
        )
        val organPart = MultipartBody.Part.createFormData("organs", "flower")
        
        val response = api.identify("fake-key", "de", listOf(imagePart), listOf(organPart))

        assertEquals(1, response.results.size)
        assertEquals(0.98, response.results[0].score, 0.001)
        assertEquals("Monstera deliciosa Liebm.", response.results[0].species.scientificName)
        assertEquals("Araceae", response.results[0].species.family.scientificNameWithoutAuthor)
        assertEquals(listOf("Swiss cheese plant"), response.results[0].species.commonNames)
    }
}
