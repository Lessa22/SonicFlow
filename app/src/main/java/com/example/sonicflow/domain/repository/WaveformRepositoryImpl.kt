package com.example.sonicflow.data.repository

import com.example.sonicflow.data.WaveformGenerator
import com.example.sonicflow.data.local.database.WaveformDao
import com.example.sonicflow.data.local.entities.WaveformDataEntity
import com.example.sonicflow.domain.repository.WaveformRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaveformRepositoryImpl @Inject constructor(
    private val waveformDao: WaveformDao,
    private val waveformGenerator: WaveformGenerator
) : WaveformRepository {

    override suspend fun getWaveform(trackId: Long, trackPath: String): List<Float> {
        // Vérifier si déjà en cache
        val cached = waveformDao.getWaveformData(trackId)
        if (cached != null) {
            return cached.amplitudes.split(",").map { it.toFloatOrNull() ?: 0.5f }
        }

        // Générer la waveform
        val amplitudes = waveformGenerator.generateWaveform(trackPath)

        // Sauvegarder en cache
        waveformDao.insertWaveformData(
            WaveformDataEntity(
                trackId = trackId,
                amplitudes = amplitudes.joinToString(",")
            )
        )

        return amplitudes
    }

    override suspend fun clearCache() {
        waveformDao.clearAllWaveformData()
    }
}