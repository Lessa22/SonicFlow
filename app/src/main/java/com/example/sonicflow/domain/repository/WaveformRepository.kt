package com.example.sonicflow.domain.repository

interface WaveformRepository {
    suspend fun getWaveform(trackId: Long, trackPath: String): List<Float>
    suspend fun clearCache()
}