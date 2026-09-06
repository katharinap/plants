package com.katharina.plants.ui.plantid

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: PlantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlantIdUiState>(PlantIdUiState.Idle)
    val uiState: StateFlow<PlantIdUiState> = _uiState.asStateFlow()

    private val _selectedUri = MutableStateFlow<Uri?>(null)
    val selectedUri: StateFlow<Uri?> = _selectedUri.asStateFlow()

    fun onImageSelected(uri: Uri?) {
        _selectedUri.value = uri
        _uiState.value = PlantIdUiState.Idle
    }

    fun identifyPlants(organs: List<Organ> = emptyList()) {
        val uri = _selectedUri.value ?: return

        viewModelScope.launch {
            _uiState.value = PlantIdUiState.Loading
            repository.identify(listOf(ImageInput(uri)), organs)
                .onSuccess { results ->
                    _uiState.value = PlantIdUiState.Success(results)
                }
                .onFailure { error ->
                    _uiState.value = PlantIdUiState.Error(error.message ?: "Unknown error occurred")
                }
        }
    }
}
