package com.katharina.plants.ui.history

import androidx.compose.foundation.clickable
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
import com.katharina.plants.data.local.entity.IdentificationEntity
import com.katharina.plants.ui.theme.PlantsTheme
import java.text.DateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onItemClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val identifications by viewModel.identifications.collectAsState()

    HistoryContent(
        identifications = identifications,
        onItemClick = onItemClick,
        onDeleteClick = viewModel::deleteIdentification,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    identifications: List<IdentificationEntity>,
    onItemClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Identification History") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (identifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No identifications yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(identifications, key = { it.id }) { entity ->
                    HistoryItem(
                        entity = entity,
                        onItemClick = { onItemClick(entity.id) },
                        onDeleteClick = { onDeleteClick(entity.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    entity: IdentificationEntity,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min)
        ) {
            AsyncImage(
                model = entity.imagePath,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = entity.speciesName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (entity.commonNames.isNotEmpty()) {
                    Text(
                        text = entity.commonNames,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = DateFormat.getDateTimeInstance().format(Date(entity.timestamp)),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            TextButton(onClick = onDeleteClick) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    PlantsTheme {
        HistoryContent(
            identifications = listOf(
                IdentificationEntity(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    imagePath = "",
                    speciesName = "Monstera deliciosa",
                    scientificName = "Monstera deliciosa Liebm.",
                    commonNames = "Swiss cheese plant",
                    confidenceScore = 0.98
                )
            ),
            onItemClick = {},
            onDeleteClick = {},
            onBackClick = {}
        )
    }
}
