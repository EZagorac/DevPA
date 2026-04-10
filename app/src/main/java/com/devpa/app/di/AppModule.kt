package com.devpa.app.di

import android.content.Context
import androidx.room.Room
import com.devpa.app.data.db.DevPADatabase
import com.devpa.app.data.db.HabitDao
import com.devpa.app.data.db.JourneyDao
import com.devpa.app.data.db.JourneyStepDao
import com.devpa.app.data.repository.JourneyPrefsRepository
import com.devpa.app.data.repository.JourneyRepository
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
        .addMigrations(DevPADatabase.MIGRATION_1_2, DevPADatabase.MIGRATION_2_3)
        .build()
    }

    @Provides
    fun provideHabitDao(db: DevPADatabase): HabitDao = db.habitDao()

    @Provides
    fun provideJourneyDao(db: DevPADatabase): JourneyDao = db.journeyDao()

    @Provides
    fun provideJourneyStepDao(db: DevPADatabase): JourneyStepDao = db.journeyStepDao()

    @Provides
    @Singleton
    fun provideJourneyPrefsRepository(@ApplicationContext context: Context): JourneyPrefsRepository =
        JourneyPrefsRepository(context)

    @Provides
    @Singleton
    fun provideJourneyRepository(
        journeyDao: JourneyDao,
        stepDao: JourneyStepDao,
        prefsRepository: JourneyPrefsRepository
    ): JourneyRepository = JourneyRepository(journeyDao, stepDao, prefsRepository)

    @Provides
    @Singleton
    fun provideClaudeApiService(): ClaudeApiService = ClaudeApiClient.service
}
