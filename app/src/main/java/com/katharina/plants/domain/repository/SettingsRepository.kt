package com.katharina.plants.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val languageCode: Flow<String>
    suspend fun setLanguageCode(code: String)
}
