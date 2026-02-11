package com.example.sonicflow.data

import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.math.abs

object WaveformGenerator {

    private const val SAMPLES_COUNT = 100 // Nombre de barres dans la waveform

    suspend fun generateWaveform(audioPath: String): List<Float> = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()

        try {
            extractor.setDataSource(audioPath)

            // Trouver la piste audio
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
                return@withContext emptyList()
            }

            extractor.selectTrack(audioTrackIndex)

            val amplitudes = mutableListOf<Float>()
            val buffer = ByteBuffer.allocate(1024 * 16)
            val samplesList = mutableListOf<Short>()

            // Extraire tous les échantillons
            while (extractor.readSampleData(buffer, 0) >= 0) {
                buffer.rewind()

                // Lire les échantillons (16 bits = Short)
                while (buffer.remaining() >= 2) {
                    val sample = buffer.short
                    samplesList.add(sample)
                }

                buffer.clear()
                extractor.advance()
            }

            if (samplesList.isEmpty()) {
                return@withContext List(SAMPLES_COUNT) { 0.5f }
            }

            // Diviser en segments et calculer l'amplitude moyenne
            val samplesPerSegment = samplesList.size / SAMPLES_COUNT

            for (i in 0 until SAMPLES_COUNT) {
                val start = i * samplesPerSegment
                val end = minOf(start + samplesPerSegment, samplesList.size)

                if (start >= samplesList.size) break

                // Calculer l'amplitude moyenne pour ce segment
                var sum = 0f
                for (j in start until end) {
                    sum += abs(samplesList[j].toFloat())
                }

                val average = sum / (end - start)
                val normalized = (average / Short.MAX_VALUE).coerceIn(0.1f, 1f)
                amplitudes.add(normalized)
            }

            // Remplir le reste si nécessaire
            while (amplitudes.size < SAMPLES_COUNT) {
                amplitudes.add(0.3f)
            }

            return@withContext amplitudes

        } catch (e: Exception) {
            android.util.Log.e("WaveformGenerator", "Error generating waveform", e)
            // Retourner une waveform par défaut en cas d'erreur
            return@withContext List(SAMPLES_COUNT) { 0.5f }
        } finally {
            extractor.release()
        }
    }
}