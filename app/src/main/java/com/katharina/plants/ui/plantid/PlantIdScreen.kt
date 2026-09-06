package com.katharina.plants.ui.plantid

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.katharina.plants.domain.model.PlantIdentificationResult
import com.katharina.plants.ui.theme.PlantsTheme

@Composable
fun PlantIdScreen(
    viewModel: PlantIdViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedUri by viewModel.selectedUri.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onImageSelected(uri)
    }

    PlantIdContent(
        uiState = uiState,
        selectedUri = selectedUri,
        onPickImageClick = {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onIdentifyClick = {
            viewModel.identifyPlants()
        },
        modifier = modifier
    )
}

@Composable
fun PlantIdContent(
    uiState: PlantIdUiState,
    selectedUri: Uri?,
    onPickImageClick: () -> Unit,
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
        // Selected image or placeholder
        Box(
            modifier =
                Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (selectedUri != null) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected plant image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("No image selected")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(onClick = onPickImageClick) {
                Text("Pick Image")
            }

            Button(
                onClick = onIdentifyClick,
                enabled = selectedUri != null && uiState !is PlantIdUiState.Loading
            ) {
                Text("Identify")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is PlantIdUiState.Idle -> {
                if (selectedUri != null) {
                    Text("Ready to identify")
                } else {
                    Text("Select an image to identify")
                }
            }

            is PlantIdUiState.Loading -> {
                CircularProgressIndicator()
            }

            is PlantIdUiState.Success -> {
                if (uiState.results.isEmpty()) {
                    Text("No plants identified. Try another photo.")
                } else {
                    PlantResultList(results = uiState.results)
                }
            }

            is PlantIdUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onIdentifyClick) {
                        Text("Retry")
                    }
                }
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
            selectedUri = null,
            onPickImageClick = {},
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
            selectedUri = null,
            onPickImageClick = {},
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
            selectedUri = null,
            onPickImageClick = {},
            onIdentifyClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantIdScreenEmptyPreview() {
    PlantsTheme {
        PlantIdContent(
            uiState = PlantIdUiState.Success(results = emptyList()),
            selectedUri = null,
            onPickImageClick = {},
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
            selectedUri = null,
            onPickImageClick = {},
            onIdentifyClick = {}
        )
    }
}
