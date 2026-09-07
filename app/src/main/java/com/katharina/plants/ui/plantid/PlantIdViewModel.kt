package com.katharina.plants.ui.plantid

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.local.entity.IdentificationEntity
import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import com.katharina.plants.domain.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantIdViewModel @Inject constructor(
    private val repository: PlantRepository,
    private val dao: IdentificationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlantIdUiState>(PlantIdUiState.Idle)
    val uiState: StateFlow<PlantIdUiState> = _uiState.asStateFlow()

    private val _selectedUri = MutableStateFlow<Uri?>(null)
    val selectedUri: StateFlow<Uri?> = _selectedUri.asStateFlow()

    private val _selectedOrgan = MutableStateFlow<Organ>(Organ.FLOWER)
    val selectedOrgan: StateFlow<Organ> = _selectedOrgan.asStateFlow()

    fun onImageSelected(uri: Uri?) {
        _selectedUri.value = uri
        _uiState.value = PlantIdUiState.Idle
    }

    fun onOrganSelected(organ: Organ) {
        _selectedOrgan.value = organ
    }

    fun identifyPlants() {
        val uri = _selectedUri.value ?: return
        val organ = _selectedOrgan.value

        viewModelScope.launch {
            _uiState.value = PlantIdUiState.Loading
            repository.identify(listOf(ImageInput(uri)), listOf(organ))
                .onSuccess { results ->
                    _uiState.value = PlantIdUiState.Success(results)
                    
                    // Save top result to history
                    if (results.isNotEmpty()) {
                        val topResult = results[0]
                        dao.insertIdentification(
                            IdentificationEntity(
                                timestamp = System.currentTimeMillis(),
                                imagePath = uri.toString(),
                                speciesName = topResult.speciesName,
                                scientificName = topResult.scientificName,
                                confidenceScore = topResult.confidenceScore
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = PlantIdUiState.Error(error.message ?: "Unknown error occurred")
                }
        }
    }
}
