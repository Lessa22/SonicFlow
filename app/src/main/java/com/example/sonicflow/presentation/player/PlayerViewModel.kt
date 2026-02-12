package com.example.sonicflow.presentation.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.MusicRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import com.example.sonicflow.domain.repository.WaveformRepository
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
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val currentTrack: Track? = null,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val waveformAmplitudes: List<Float> = emptyList(),
    val isLoadingWaveform: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val playlists: List<Playlist> = emptyList()
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val musicController: MusicController,
    private val playlistRepository: PlaylistRepository,
    private val waveformRepository: WaveformRepository
) : ViewModel() {

    private val trackId: Long = savedStateHandle.get<Long>("trackId") ?: 0L

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressUpdateJob: Job? = null

    init {
        loadTrack()
        observePlayerState()
        startProgressUpdate()
        loadPlaylists()
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

                    // Ne jouer QUE si ce n'est pas déjà la chanson en cours
                    val currentlyPlaying = musicController.playerState.value.currentTrack
                    if (currentlyPlaying == null || currentlyPlaying.id != track.id) {
                        musicController.playTracks(tracks, trackIndex)
                    }

                    // Load waveform
                    loadWaveform(track)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadWaveform(track: Track) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWaveform = true) }
            try {
                val amplitudes = waveformRepository.getWaveformData(track.id, track.path)
                _uiState.update {
                    it.copy(
                        waveformAmplitudes = amplitudes,
                        isLoadingWaveform = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "Error loading waveform", e)
                _uiState.update { it.copy(isLoadingWaveform = false) }
            }
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            musicController.playerState.collect { playerState ->
                _uiState.update {
                    it.copy(
                        currentTrack = playerState.currentTrack ?: it.currentTrack,
                        isPlaying = playerState.isPlaying,
                        duration = playerState.duration.coerceAtLeast(0L)
                    )
                }

                // Charger la waveform si la chanson a changé
                playerState.currentTrack?.let { newTrack ->
                    if (newTrack.id != _uiState.value.currentTrack?.id) {
                        loadWaveform(newTrack)
                    }
                }
            }
        }
    }

    private fun startProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                delay(100) // Mise à jour toutes les 100ms pour une waveform fluide
                val playerState = musicController.playerState.value

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

    fun showAddToPlaylistDialog() {
        _uiState.update { it.copy(showAddToPlaylistDialog = true) }
    }

    fun hideAddToPlaylistDialog() {
        _uiState.update { it.copy(showAddToPlaylistDialog = false) }
    }

    fun addToPlaylist(playlistId: Long) {
        val track = _uiState.value.currentTrack ?: return

        viewModelScope.launch {
            try {
                playlistRepository.addTrackToPlaylist(playlistId, track.id)
                android.util.Log.d("PlayerViewModel", "Added '${track.title}' to playlist $playlistId")
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "Error adding to playlist", e)
            }
        }

        _uiState.update { it.copy(showAddToPlaylistDialog = false) }
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