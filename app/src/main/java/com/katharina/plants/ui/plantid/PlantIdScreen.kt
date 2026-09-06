package com.katharina.plants.ui.plantid

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.PlantIdentificationResult
import com.katharina.plants.ui.theme.PlantsTheme

@Composable
fun PlantIdScreen(
    viewModel: PlantIdViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    PlantIdContent(
        uiState = uiState,
        onIdentifyClick = {
            // Trigger with a fake URI for now as per Step C1
            viewModel.identifyPlants(listOf(ImageInput(Uri.EMPTY)))
        },
        modifier = modifier
    )
}

@Composable
fun PlantIdContent(
    uiState: PlantIdUiState,
    onIdentifyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Placeholder for image
        Box(
            modifier =
                Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Image Placeholder")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(onClick = { /* Stub for Photo Picker */ }) {
                Text("Pick Image")
            }

            Button(
                onClick = onIdentifyClick,
            ) {
                Text("Identify")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is PlantIdUiState.Idle -> {
                Text("Select an image to identify")
            }

            is PlantIdUiState.Loading -> {
                CircularProgressIndicator()
            }

            is PlantIdUiState.Success -> {
                PlantResultList(results = uiState.results)
            }

            is PlantIdUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun PlantResultList(results: List<PlantIdentificationResult>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(results) { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = result.speciesName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = result.scientificName,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "Confidence: ${(result.confidenceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlantIdScreenIdlePreview() {
    PlantsTheme {
        PlantIdContent(
            uiState = PlantIdUiState.Idle,
            onIdentifyClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantIdScreenLoadingPreview() {
    PlantsTheme {
        PlantIdContent(
            uiState = PlantIdUiState.Loading,
            onIdentifyClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantIdScreenSuccessPreview() {
    PlantsTheme {
        PlantIdContent(
            uiState = PlantIdUiState.Success(
                results = listOf(
                    PlantIdentificationResult(
                        speciesName = "Monstera deliciosa",
                        scientificName = "Monstera deliciosa Liebm.",
                        commonNames = listOf("Swiss cheese plant"),
                        confidenceScore = 0.98,
                        family = "Araceae",
                        thumbnailUrl = null
                    )
                )
            ),
            onIdentifyClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantIdScreenErrorPreview() {
    PlantsTheme {
        PlantIdContent(
            uiState = PlantIdUiState.Error("Failed to connect to server"),
            onIdentifyClick = {}
        )
    }
}
