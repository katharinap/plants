package com.katharina.plants.ui.plantid

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.katharina.plants.domain.model.Organ
import com.katharina.plants.domain.model.PlantIdentificationResult
import com.katharina.plants.ui.camera.CameraCaptureScreen
import com.katharina.plants.ui.theme.PlantsTheme
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantIdScreen(
    viewModel: PlantIdViewModel,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedUri by viewModel.selectedUri.collectAsState()
    val selectedOrgan by viewModel.selectedOrgan.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isCameraVisible by remember { mutableStateOf(false) }

    val pickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            viewModel.onImageSelected(uri)
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                isCameraVisible = true
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Camera permission is required to take photos.")
                }
            }
        }

    if (isCameraVisible) {
        CameraCaptureScreen(
            onImageCaptured = { uri ->
                viewModel.onImageSelected(uri)
                isCameraVisible = false
            },
            onClose = {
                isCameraVisible = false
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Plant Identifier") },
                    actions = {
                        TextButton(onClick = onHistoryClick) {
                            Text("History")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = modifier,
        ) { innerPadding ->
            PlantIdContent(
                uiState = uiState,
                selectedUri = selectedUri,
                selectedOrgan = selectedOrgan,
                onPickImageClick = {
                    pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onTakePhotoClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onOrganSelected = viewModel::onOrganSelected,
                onIdentifyClick = {
                    viewModel.identifyPlants()
                },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
fun PlantIdContent(
    uiState: PlantIdUiState,
    selectedUri: Uri?,
    selectedOrgan: Organ,
    onPickImageClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onOrganSelected: (Organ) -> Unit,
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
                    contentScale = ContentScale.Crop,
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

            Button(onClick = onTakePhotoClick) {
                Text("Take Photo")
            }
        }

        if (selectedUri != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select plant organ:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Organ.entries.forEach { organ ->
                    FilterChip(
                        selected = selectedOrgan == organ,
                        onClick = { onOrganSelected(organ) },
                        label = {
                            Text(organ.name.lowercase().replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            })
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onIdentifyClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedUri != null && uiState !is PlantIdUiState.Loading,
        ) {
            Text("Identify")
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
                        style = MaterialTheme.typography.bodyMedium,
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
                    if (result.commonNames.isNotEmpty()) {
                        Text(
                            text = result.commonNames.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
            selectedOrgan = Organ.FLOWER,
            onPickImageClick = {},
            onTakePhotoClick = {},
            onOrganSelected = {},
            onIdentifyClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantIdScreenWithOrganSelectionPreview() {
    PlantsTheme {
        PlantIdContent(
            uiState = PlantIdUiState.Idle,
            selectedUri = Uri.parse("fake"),
            selectedOrgan = Organ.LEAF,
            onPickImageClick = {},
            onTakePhotoClick = {},
            onOrganSelected = {},
            onIdentifyClick = {},
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
            selectedOrgan = Organ.FLOWER,
            onPickImageClick = {},
            onTakePhotoClick = {},
            onOrganSelected = {},
            onIdentifyClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantIdScreenSuccessPreview() {
    PlantsTheme {
        PlantIdContent(
            uiState =
                PlantIdUiState.Success(
                    results =
                        listOf(
                            PlantIdentificationResult(
                                speciesName = "Monstera deliciosa",
                                scientificName = "Monstera deliciosa Liebm.",
                                commonNames = listOf("Swiss cheese plant"),
                                confidenceScore = 0.98,
                                family = "Araceae",
                                thumbnailUrl = null,
                            ),
                        ),
                ),
            selectedUri = null,
            selectedOrgan = Organ.FLOWER,
            onPickImageClick = {},
            onTakePhotoClick = {},
            onOrganSelected = {},
            onIdentifyClick = {},
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
            selectedOrgan = Organ.FLOWER,
            onPickImageClick = {},
            onTakePhotoClick = {},
            onOrganSelected = {},
            onIdentifyClick = {},
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
            selectedOrgan = Organ.FLOWER,
            onPickImageClick = {},
            onTakePhotoClick = {},
            onOrganSelected = {},
            onIdentifyClick = {},
        )
    }
}
