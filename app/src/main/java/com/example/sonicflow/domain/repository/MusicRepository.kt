package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getAllTracks(): List<Track>
    fun searchTracks(query: String): Flow<List<Track>>
}