package com.katharina.plants.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.local.entity.IdentificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dao: IdentificationDao
) : ViewModel() {

    val identifications: StateFlow<List<IdentificationEntity>> = dao.getAllIdentifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteIdentification(id: Long) {
        viewModelScope.launch {
            dao.deleteIdentification(id)
        }
    }
}
