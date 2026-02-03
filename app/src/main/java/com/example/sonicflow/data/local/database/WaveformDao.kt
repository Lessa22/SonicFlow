package com.example.sonicflow.data.local.database

import androidx.room.*
import com.example.sonicflow.data.local.entities.WaveformDataEntity

@Dao
interface WaveformDao {

    @Query("SELECT * FROM waveform_data WHERE trackId = :trackId")
    suspend fun getWaveformData(trackId: Long): WaveformDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaveformData(waveformData: WaveformDataEntity)

    @Query("DELETE FROM waveform_data WHERE trackId = :trackId")
    suspend fun deleteWaveformData(trackId: Long)

    @Query("DELETE FROM waveform_data")
    suspend fun clearAllWaveformData()
}