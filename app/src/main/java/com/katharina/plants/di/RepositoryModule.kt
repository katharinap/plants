package com.katharina.plants.di

import com.katharina.plants.data.repository.FakePlantRepository
import com.katharina.plants.domain.repository.PlantRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePlantRepository(): PlantRepository {
        // Binding to Fake for now as per Step B3
        return FakePlantRepository()
    }
}
