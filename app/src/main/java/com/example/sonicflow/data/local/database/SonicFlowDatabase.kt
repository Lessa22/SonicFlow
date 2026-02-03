package com.example.sonicflow.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sonicflow.data.local.entities.PlaylistEntity
import com.example.sonicflow.data.local.entities.PlaylistTrackCrossRef
import com.example.sonicflow.data.local.entities.WaveformDataEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        WaveformDataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SonicFlowDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun waveformDao(): WaveformDao
}