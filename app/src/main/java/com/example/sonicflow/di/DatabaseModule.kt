package com.example.sonicflow.di

import android.content.Context
import androidx.room.Room
import com.example.sonicflow.data.local.database.PlaylistDao
import com.example.sonicflow.data.local.database.SonicFlowDatabase
import com.example.sonicflow.data.local.database.WaveformDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSonicFlowDatabase(
        @ApplicationContext context: Context
    ): SonicFlowDatabase {
        return Room.databaseBuilder(
            context,
            SonicFlowDatabase::class.java,
            "sonicflow_database"
        ).build()
    }

    @Provides
    fun providePlaylistDao(database: SonicFlowDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideWaveformDao(database: SonicFlowDatabase): WaveformDao {
        return database.waveformDao()
    }
}