package com.example.sonicflow.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waveform_data")
data class WaveformDataEntity(
    @PrimaryKey
    val trackId: Long,
    val amplitudes: String,
    val generatedAt: Long = System.currentTimeMillis()
)