package com.katharina.plants.ui.plantid

import com.katharina.plants.domain.model.PlantIdentificationResult

sealed interface PlantIdUiState {
    object Idle : PlantIdUiState
    object Loading : PlantIdUiState
    object Offline : PlantIdUiState
    data class Success(val results: List<PlantIdentificationResult>) : PlantIdUiState
    data class Error(val message: String) : PlantIdUiState
}
