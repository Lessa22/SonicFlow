package com.example.sonicflow.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.MusicRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import com.example.sonicflow.service.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val filteredTracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.TITLE_ASC,
    val playlists: List<Playlist> = emptyList(),
    val showAddToPlaylistDialog: Boolean = false,
    val selectedTrack: Track? = null,
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false
)

enum class SortOption {
    TITLE_ASC, TITLE_DESC, DATE_ADDED, DURATION
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val musicController: MusicController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadTracks()
        loadPlaylists()
        observeMusicController()
    }

    private fun observeMusicController() {
        viewModelScope.launch {
            musicController.playerState.collect { playerState ->
                _uiState.update {
                    it.copy(
                        currentTrack = playerState.currentTrack,
                        isPlaying = playerState.isPlaying
                    )
                }
            }
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tracks = musicRepository.getAllTracks()
            _uiState.update {
                it.copy(
                    tracks = tracks,
                    filteredTracks = tracks,
                    isLoading = false
                )
            }
            applyFiltersAndSort()
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun onSortOptionChange(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val currentState = _uiState.value
        var filtered = currentState.tracks

        // Filtrer par recherche
        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(currentState.searchQuery, ignoreCase = true) ||
                        it.artist.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        // Trier
        filtered = when (currentState.sortOption) {
            SortOption.TITLE_ASC -> filtered.sortedBy { it.title }
            SortOption.TITLE_DESC -> filtered.sortedByDescending { it.title }
            SortOption.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
            SortOption.DURATION -> filtered.sortedByDescending { it.duration }
        }

        _uiState.update { it.copy(filteredTracks = filtered) }
    }

    fun showAddToPlaylistDialog(track: Track) {
        _uiState.update {
            it.copy(
                showAddToPlaylistDialog = true,
                selectedTrack = track
            )
        }
    }

    fun hideAddToPlaylistDialog() {
        _uiState.update {
            it.copy(
                showAddToPlaylistDialog = false,
                selectedTrack = null
            )
        }
    }

    fun addToPlaylist(playlistId: Long) {
        val track = _uiState.value.selectedTrack ?: return

        viewModelScope.launch {
            try {
                playlistRepository.addTrackToPlaylist(playlistId, track.id)
                android.util.Log.d("LibraryViewModel", "Added '${track.title}' to playlist $playlistId")
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error adding to playlist", e)
            }
        }

        hideAddToPlaylistDialog()
    }

    fun onPlayPauseClick() {
        musicController.playPause()
    }

    fun onNextClick() {
        musicController.skipToNext()
    }
}