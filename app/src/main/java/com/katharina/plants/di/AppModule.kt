package com.katharina.plants.di

import android.content.Context
import androidx.room.Room
import com.katharina.plants.data.local.PlantsDatabase
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.util.ImageOptimizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePlantsDatabase(@ApplicationContext context: Context): PlantsDatabase {
        return Room.databaseBuilder(
            context,
            PlantsDatabase::class.java,
            "plants.db"
        ).build()
    }

    @Provides
    fun provideIdentificationDao(db: PlantsDatabase): IdentificationDao {
        return db.dao
    }

    @Provides
    @Singleton
    fun provideImageOptimizer(@ApplicationContext context: Context): ImageOptimizer {
        return ImageOptimizer(context)
    }
}
