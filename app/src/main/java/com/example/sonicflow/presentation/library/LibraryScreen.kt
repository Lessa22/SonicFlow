package com.example.sonicflow.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen(
    onTrackClick: (Long) -> Unit,
    onPlaylistsClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.READ_MEDIA_AUDIO
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Music") },
                actions = {
                    IconButton(onClick = onPlaylistsClick) {
                        Icon(Icons.Default.PlaylistPlay, "Playlists")
                    }
                }
            )
        },
        bottomBar = {
            // Mini Player - s'affiche seulement s'il y a une chanson en cours
            if (uiState.currentTrack != null) {
                MiniPlayer(
                    currentTrack = uiState.currentTrack,
                    isPlaying = uiState.isPlaying,
                    onPlayPauseClick = { viewModel.onPlayPauseClick() },
                    onNextClick = { viewModel.onNextClick() },
                    onPlayerClick = {
                        uiState.currentTrack?.let { track ->
                            onTrackClick(track.id)
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search songs...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            // Sort Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortChip(
                    label = "A-Z",
                    selected = uiState.sortOption == SortOption.TITLE_ASC,
                    onClick = { viewModel.onSortOptionChange(SortOption.TITLE_ASC) }
                )
                SortChip(
                    label = "Z-A",
                    selected = uiState.sortOption == SortOption.TITLE_DESC,
                    onClick = { viewModel.onSortOptionChange(SortOption.TITLE_DESC) }
                )
                SortChip(
                    label = "Date",
                    selected = uiState.sortOption == SortOption.DATE_ADDED,
                    onClick = { viewModel.onSortOptionChange(SortOption.DATE_ADDED) }
                )
                SortChip(
                    label = "Duration",
                    selected = uiState.sortOption == SortOption.DURATION,
                    onClick = { viewModel.onSortOptionChange(SortOption.DURATION) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tracks List
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.filteredTracks.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No songs found")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = if (uiState.currentTrack != null) 80.dp else 0.dp)
                    ) {
                        items(uiState.filteredTracks) { track ->
                            TrackItemWithMenu(
                                track = track,
                                onClick = { onTrackClick(track.id) },
                                onAddToPlaylist = { viewModel.showAddToPlaylistDialog(track) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogue d'ajout à une playlist
    if (uiState.showAddToPlaylistDialog && uiState.selectedTrack != null) {
        AddToPlaylistDialog(
            playlists = uiState.playlists,
            onPlaylistSelected = { playlistId ->
                viewModel.addToPlaylist(playlistId)
            },
            onDismiss = { viewModel.hideAddToPlaylistDialog() }
        )
    }
}

@Composable
fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItemWithMenu(
    track: Track,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${track.artist} • ${formatDuration(track.duration)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Card(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.small
            ) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "Album art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        },
        trailingContent = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to playlist") },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        },
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, null) }
                    )
                }
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onPlaylistSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (playlists.isEmpty()) {
                    Text("No playlists yet. Create one first!")
                } else {
                    Text("Select a playlist:")
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        playlists.forEach { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onPlaylistSelected(playlist.id)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PlaylistPlay,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${playlist.trackCount} songs",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    return String.format("%d:%02d", minutes, seconds)
}