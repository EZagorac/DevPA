package com.devpa.app.di

import android.content.Context
import androidx.room.Room
import com.devpa.app.data.db.DevPADatabase
import com.devpa.app.data.db.HabitDao
import com.devpa.app.data.db.PortfolioDao
import com.devpa.app.data.repository.ClaudeApiClient
import com.devpa.app.data.repository.ClaudeApiService
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
    fun provideDatabase(@ApplicationContext context: Context): DevPADatabase {
        return Room.databaseBuilder(
            context,
            DevPADatabase::class.java,
            "dev_pa.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideHabitDao(db: DevPADatabase): HabitDao = db.habitDao()

    @Provides
    fun providePortfolioDao(db: DevPADatabase): PortfolioDao = db.portfolioDao()

    @Provides
    @Singleton
    fun provideClaudeApiService(): ClaudeApiService = ClaudeApiClient.service
}
