package com.katharina.plants.ui.plantid

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.local.entity.IdentificationEntity
import com.katharina.plants.data.util.ConnectivityObserver
import com.katharina.plants.domain.model.ImageInput
import com.katharina.plants.domain.model.Organ
import com.katharina.plants.domain.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class PlantIdViewModel @Inject constructor(
    private val repository: PlantRepository,
    private val dao: IdentificationDao,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlantIdUiState>(PlantIdUiState.Idle)
    val uiState: StateFlow<PlantIdUiState> = _uiState.asStateFlow()

    private val _selectedUri = MutableStateFlow<Uri?>(null)
    val selectedUri: StateFlow<Uri?> = _selectedUri.asStateFlow()

    private val _selectedOrgan = MutableStateFlow<Organ>(Organ.FLOWER)
    val selectedOrgan: StateFlow<Organ> = _selectedOrgan.asStateFlow()

    val networkStatus = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectivityObserver.Status.Unavailable)

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

        if (networkStatus.value != ConnectivityObserver.Status.Available) {
            _uiState.value = PlantIdUiState.Offline
            return
        }

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
                                commonNames = topResult.commonNames.joinToString(", "),
                                confidenceScore = topResult.confidenceScore
                            )
                        )
                    }
                }
                .onFailure { error ->
                    val message = when (error) {
                        is UnknownHostException -> "No internet connection"
                        is SocketTimeoutException -> "Request timed out"
                        else -> error.message ?: "Unknown error occurred"
                    }
                    _uiState.value = PlantIdUiState.Error(message)
                }
        }
    }
}
