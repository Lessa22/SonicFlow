package com.example.sonicflow.data.repository

import com.example.sonicflow.data.WaveformGenerator
import com.example.sonicflow.data.local.database.WaveformDao
import com.example.sonicflow.data.local.entities.WaveformDataEntity
import com.example.sonicflow.domain.repository.WaveformRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WaveformRepositoryImpl @Inject constructor(
    private val waveformDao: WaveformDao
) : WaveformRepository {

    override suspend fun getWaveformData(trackId: Long, audioPath: String): List<Float> = withContext(Dispatchers.IO) {
        // Vérifier si on a déjà la waveform en cache
        val cached = waveformDao.getWaveformData(trackId)

        if (cached != null) {
            return@withContext parseAmplitudes(cached.amplitudesData)
        }

        // Sinon, générer la waveform
        val amplitudes = WaveformGenerator.generateWaveform(audioPath)

        // Sauvegarder en cache
        waveformDao.insertWaveformData(
            WaveformDataEntity(
                trackId = trackId,
                amplitudesData = amplitudes.joinToString(",")
            )
        )


        return@withContext amplitudes
    }

    private fun parseAmplitudes(data: String): List<Float> {
        return try {
            data.split(",").map { it.toFloat() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}