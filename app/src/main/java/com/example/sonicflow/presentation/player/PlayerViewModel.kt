package com.example.sonicflow.presentation.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.MusicRepository
import com.example.sonicflow.service.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isLoading: Boolean = false,
    val waveformAmplitudes: List<Float> = emptyList(),
    val isLoadingWaveform: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val musicController: MusicController
) : ViewModel() {

    private val trackId: Long = savedStateHandle.get<String>("trackId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressUpdateJob: Job? = null

    init {
        loadTrack()
        observePlayerState()
        startProgressUpdate()
    }

    private fun loadTrack() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tracks = musicRepository.getAllTracks()
                val trackIndex = tracks.indexOfFirst { it.id == trackId }

                if (trackIndex != -1) {
                    val track = tracks[trackIndex]
                    _uiState.update {
                        it.copy(
                            currentTrack = track,
                            isLoading = false
                        )
                    }
                    // Play the track
                    musicController.playTracks(tracks, trackIndex)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            musicController.playerState.collect { playerState ->
                _uiState.update {
                    it.copy(
                        isPlaying = playerState.isPlaying,
                        duration = playerState.duration.coerceAtLeast(0L)
                    )
                }
            }
        }
    }

    private fun startProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                delay(200) // 200ms pour un mouvement plus fluide
                val playerState = musicController.playerState.value

                // Log pour debug
                android.util.Log.d("PlayerViewModel", "Position: ${playerState.currentPosition}, Duration: ${playerState.duration}, IsPlaying: ${playerState.isPlaying}")

                // Toujours mettre à jour, même si pas en lecture
                _uiState.update {
                    it.copy(
                        currentPosition = playerState.currentPosition.coerceAtLeast(0L),
                        duration = playerState.duration.coerceAtLeast(0L),
                        isPlaying = playerState.isPlaying
                    )
                }
            }
        }
    }

    fun onPlayPauseClick() {
        musicController.playPause()
    }

    fun onSeekTo(position: Long) {
        musicController.seekTo(position)
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun onNextClick() {
        musicController.skipToNext()
    }

    fun onPreviousClick() {
        musicController.skipToPrevious()
    }

    override fun onCleared() {
        super.onCleared()
        progressUpdateJob?.cancel()
    }
}