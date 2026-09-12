package com.katharina.plants.di

import com.katharina.plants.data.repository.PlantNetRepository
import com.katharina.plants.data.repository.SettingsRepositoryImpl
import com.katharina.plants.domain.repository.PlantRepository
import com.katharina.plants.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlantRepository(
        plantNetRepository: PlantNetRepository
    ): PlantRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
