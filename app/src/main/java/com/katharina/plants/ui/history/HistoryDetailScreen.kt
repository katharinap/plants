package com.katharina.plants.ui.history

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.katharina.plants.R
import com.katharina.plants.data.local.entity.IdentificationEntity
import java.io.File
import java.text.DateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    viewModel: HistoryDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val identification by viewModel.identification.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Identification Detail") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                },
                actions = {
                    identification?.let { entity ->
                        IconButton(onClick = { shareIdentification(context, entity) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_share_outlined),
                                contentDescription = "Share",
                            )
                        }
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        identification?.let { entity ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = entity.imagePath,
                    contentDescription = "Identified plant",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    contentScale = ContentScale.Fit,
                )

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text(
                        text = entity.speciesName,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    if (entity.commonNames.isNotEmpty()) {
                        Text(
                            text = entity.commonNames,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scientific Name: ${entity.scientificName}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Confidence: ${(entity.confidenceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Date: ${DateFormat.getDateTimeInstance().format(Date(entity.timestamp))}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

private fun shareIdentification(
    context: Context,
    entity: IdentificationEntity,
) {
    val imageFile = File(entity.imagePath)
    if (!imageFile.exists()) return

    val contentUri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )

    val shareText =
        """
        Check out this plant I identified!
        
        Species: ${entity.speciesName}
        ${if (entity.commonNames.isNotEmpty()) "Common Names: ${entity.commonNames}" else ""}
        Scientific Name: ${entity.scientificName}
        Confidence: ${(entity.confidenceScore * 100).toInt()}%
        Date: ${DateFormat.getDateTimeInstance().format(Date(entity.timestamp))}
        """.trimIndent()

    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    context.startActivity(Intent.createChooser(shareIntent, "Share Plant Identification"))
}
