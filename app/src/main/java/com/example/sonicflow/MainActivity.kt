package com.example.sonicflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sonicflow.presentation.auth.SignInScreen
import com.example.sonicflow.presentation.auth.SignUpScreen
import com.example.sonicflow.presentation.library.LibraryScreen
import com.example.sonicflow.presentation.player.PlayerScreen
import com.example.sonicflow.presentation.playlist.PlaylistDetailScreen
import com.example.sonicflow.presentation.playlist.PlaylistsScreen
import com.example.sonicflow.ui.theme.SonicFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SonicFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("sonicflow_prefs", android.content.Context.MODE_PRIVATE)
    val isLoggedIn = prefs.getBoolean("is_logged_in", false)

    val startDestination = if (isLoggedIn) "library" else "signin"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable("signin") {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate("library") {
                        popUpTo("signin") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("library") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.popBackStack()
                }
            )
        }

        // Library Screen
        composable("library") {
            LibraryScreen(
                onTrackClick = { trackId ->
                    navController.navigate("player/$trackId")
                },
                onPlaylistsClick = {
                    navController.navigate("playlists")
                }
            )
        }

        // Player Screen
        composable(
            route = "player/{trackId}",
            arguments = listOf(
                navArgument("trackId") { type = NavType.LongType }
            )
        ) {
            PlayerScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Playlists Screen
        composable("playlists") {
            PlaylistsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate("playlist/$playlistId")
                }
            )
        }

        // Playlist Detail Screen
        composable(
            route = "playlist/{playlistId}",
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType }
            )
        ) {
            PlaylistDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTrackClick = { trackId ->
                    navController.navigate("player/$trackId")
                }
            )
        }
    }
}