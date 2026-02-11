package com.example.sonicflow.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waveform_data")
data class WaveformDataEntity(
    @PrimaryKey
    val trackId: Long,
    val amplitudesData: String, // Liste d'amplitudes séparées par des virgules
    val createdAt: Long = System.currentTimeMillis()
)