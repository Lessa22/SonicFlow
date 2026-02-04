package com.example.sonicflow.di

import com.example.sonicflow.data.repository.MusicRepositoryImpl
import com.example.sonicflow.data.repository.PlaylistRepositoryImpl
import com.example.sonicflow.data.repository.WaveformRepositoryImpl
import com.example.sonicflow.domain.repository.MusicRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import com.example.sonicflow.domain.repository.WaveformRepository
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
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindWaveformRepository(
        waveformRepositoryImpl: WaveformRepositoryImpl
    ): WaveformRepository
}