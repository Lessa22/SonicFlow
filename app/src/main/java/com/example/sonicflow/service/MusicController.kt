package com.example.sonicflow.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.sonicflow.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L
)

@Singleton
class MusicController @Inject constructor(
    private val player: ExoPlayer,
    @ApplicationContext private val context: Context
) {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private var currentTracks: List<Track> = emptyList()
    private var currentIndex: Int = 0

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }

                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }

                Log.d("MusicController", "onIsPlayingChanged: $isPlaying")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = player.duration.coerceAtLeast(0L)
                        _playerState.update { it.copy(duration = duration) }
                        Log.d("MusicController", "STATE_READY - Duration: $duration")
                    }
                    Player.STATE_ENDED -> {
                        skipToNext()
                    }
                }
            }
        })
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = scope.launch {
            while (true) {
                val currentPos = player.currentPosition.coerceAtLeast(0L)
                val duration = player.duration.coerceAtLeast(0L)

                _playerState.update {
                    it.copy(
                        currentPosition = currentPos,
                        duration = duration,
                        isPlaying = player.isPlaying
                    )
                }

                delay(100) // Mise à jour toutes les 100ms
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        currentTracks = tracks
        currentIndex = startIndex

        val track = tracks.getOrNull(startIndex) ?: return

        // Démarrer le service pour la notification
        startMusicService()

        val mediaItems = tracks.map { MediaItem.fromUri(it.path) }
        player.setMediaItems(mediaItems, startIndex, 0)
        player.prepare()
        player.playWhenReady = true

        _playerState.update {
            it.copy(
                currentTrack = track,
                isPlaying = true
            )
        }

        Log.d("MusicController", "Playing: ${track.title}")
    }

    private fun startMusicService() {
        val serviceIntent = Intent(context, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun playPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        _playerState.update { it.copy(currentPosition = position) }
        Log.d("MusicController", "Seek to: $position")
    }

    fun skipToNext() {
        if (currentIndex < currentTracks.size - 1) {
            currentIndex++
            player.seekToNext()
            _playerState.update {
                it.copy(currentTrack = currentTracks[currentIndex])
            }
        } else {
            // Retour au début
            currentIndex = 0
            player.seekTo(0, 0)
            _playerState.update {
                it.copy(currentTrack = currentTracks[currentIndex])
            }
        }
    }

    fun skipToPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            player.seekToPrevious()
            _playerState.update {
                it.copy(currentTrack = currentTracks[currentIndex])
            }
        }
    }

    fun release() {
        stopProgressUpdate()
        player.release()
    }
}