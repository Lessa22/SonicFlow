package com.example.sonicflow.data.repository

import com.example.sonicflow.data.MediaStoreScanner
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val mediaStoreScanner: MediaStoreScanner
) : MusicRepository {

    private var cachedTracks: List<Track> = emptyList()

    override suspend fun getAllTracks(): List<Track> {
        if (cachedTracks.isEmpty()) {
            cachedTracks = mediaStoreScanner.getAllTracks()
        }
        return cachedTracks
    }

    override fun searchTracks(query: String): Flow<List<Track>> = flow {
        val allTracks = getAllTracks()
        val filtered = if (query.isBlank()) {
            allTracks
        } else {
            allTracks.filter { track ->
                track.title.contains(query, ignoreCase = true) ||
                        track.artist.contains(query, ignoreCase = true) ||
                        track.album.contains(query, ignoreCase = true)
            }
        }
        emit(filtered)
    }
}