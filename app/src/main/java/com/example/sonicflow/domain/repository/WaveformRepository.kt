package com.example.sonicflow.domain.repository

interface WaveformRepository {
    suspend fun getWaveformData(trackId: Long, audioPath: String): List<Float>
}