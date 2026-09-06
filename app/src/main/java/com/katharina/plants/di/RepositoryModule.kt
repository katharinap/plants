package com.katharina.plants.di

import com.katharina.plants.data.repository.PlantNetRepository
import com.katharina.plants.domain.repository.PlantRepository
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
}
