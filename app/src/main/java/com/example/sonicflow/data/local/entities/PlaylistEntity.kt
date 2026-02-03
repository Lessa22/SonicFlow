package com.example.sonicflow.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sonicflow.domain.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun PlaylistEntity.toDomain(trackCount: Int = 0): Playlist {
    return Playlist(
        id = id,
        name = name,
        createdAt = createdAt,
        trackCount = trackCount
    )
}

fun Playlist.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )
}