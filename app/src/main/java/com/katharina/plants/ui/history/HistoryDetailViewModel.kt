package com.katharina.plants.ui.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.local.entity.IdentificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val dao: IdentificationDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _identification = MutableStateFlow<IdentificationEntity?>(null)
    val identification: StateFlow<IdentificationEntity?> = _identification.asStateFlow()

    init {
        val id: Long? = savedStateHandle["identificationId"]
        if (id != null) {
            viewModelScope.launch {
                _identification.value = dao.getIdentificationById(id)
            }
        }
    }
}
