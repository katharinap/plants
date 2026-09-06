package com.katharina.plants.di

import android.content.Context
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
    fun provideImageOptimizer(@ApplicationContext context: Context): ImageOptimizer {
        return ImageOptimizer(context)
    }
}
