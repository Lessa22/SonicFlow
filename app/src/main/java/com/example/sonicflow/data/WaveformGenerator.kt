package com.example.sonicflow.data

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class WaveformGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun generateWaveform(audioPath: String, samplesCount: Int = 100): List<Float> = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(audioPath)

            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex == -1) {
                return@withContext List(samplesCount) { 0.5f }
            }

            extractor.selectTrack(audioTrackIndex)

            val buffer = ByteBuffer.allocate(1024 * 16)
            val amplitudes = mutableListOf<Float>()

            var maxAmplitude = 0f

            while (extractor.readSampleData(buffer, 0) >= 0) {
                buffer.rewind()

                var sum = 0f
                var count = 0

                while (buffer.hasRemaining()) {
                    val sample = buffer.short.toFloat()
                    sum += abs(sample)
                    count++
                }

                if (count > 0) {
                    val avgAmplitude = sum / count
                    amplitudes.add(avgAmplitude)
                    if (avgAmplitude > maxAmplitude) {
                        maxAmplitude = avgAmplitude
                    }
                }

                extractor.advance()
                buffer.clear()
            }

            extractor.release()

            // Normaliser et réduire au nombre d'échantillons souhaité
            val normalizedAmplitudes = if (maxAmplitude > 0) {
                amplitudes.map { it / maxAmplitude }
            } else {
                amplitudes.map { 0.5f }
            }

            // Réduire le nombre d'échantillons
            val step = normalizedAmplitudes.size / samplesCount
            if (step < 1) {
                return@withContext normalizedAmplitudes.take(samplesCount)
            }

            List(samplesCount) { i ->
                val index = i * step
                if (index < normalizedAmplitudes.size) {
                    normalizedAmplitudes[index]
                } else {
                    0.5f
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WaveformGenerator", "Error generating waveform", e)
            List(samplesCount) { 0.5f }
        }
    }
}