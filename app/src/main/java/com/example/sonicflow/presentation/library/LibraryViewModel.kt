package com.example.sonicflow.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.MusicRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder {
    TITLE_ASC, TITLE_DESC, DATE_ADDED_DESC, DURATION_ASC
}

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val showAddToPlaylistDialog: Boolean = false,
    val selectedTrackForPlaylist: Track? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadTracks()
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    fun loadTracks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tracks = musicRepository.getAllTracks()
                _uiState.update {
                    it.copy(
                        tracks = sortTracks(tracks, it.sortOrder),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load tracks: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            musicRepository.searchTracks(query)
                .collect { tracks ->
                    _uiState.update { state ->
                        state.copy(tracks = sortTracks(tracks, state.sortOrder))
                    }
                }
        }
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        _uiState.update { state ->
            state.copy(
                sortOrder = sortOrder,
                tracks = sortTracks(state.tracks, sortOrder)
            )
        }
    }

    fun showAddToPlaylistDialog(track: Track) {
        _uiState.update {
            it.copy(
                showAddToPlaylistDialog = true,
                selectedTrackForPlaylist = track
            )
        }
    }

    fun hideAddToPlaylistDialog() {
        _uiState.update {
            it.copy(
                showAddToPlaylistDialog = false,
                selectedTrackForPlaylist = null
            )
        }
    }

    fun addTrackToPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _uiState.value.selectedTrackForPlaylist?.let { track ->
                playlistRepository.addTrackToPlaylist(playlistId, track.id)
            }
        }
    }

    private fun sortTracks(tracks: List<Track>, sortOrder: SortOrder): List<Track> {
        return when (sortOrder) {
            SortOrder.TITLE_ASC -> tracks.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> tracks.sortedByDescending { it.title.lowercase() }
            SortOrder.DATE_ADDED_DESC -> tracks.sortedByDescending { it.dateAdded }
            SortOrder.DURATION_ASC -> tracks.sortedBy { it.duration }
        }
    }
}