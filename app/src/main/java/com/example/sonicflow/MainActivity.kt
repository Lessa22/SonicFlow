package com.example.sonicflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sonicflow.data.PreferencesManager
import com.example.sonicflow.presentation.Screen
import com.example.sonicflow.presentation.auth.SignInScreen
import com.example.sonicflow.presentation.auth.SignUpScreen
import com.example.sonicflow.presentation.library.LibraryScreen
import com.example.sonicflow.presentation.player.PlayerScreen
import com.example.sonicflow.presentation.playlist.PlaylistDetailScreen
import com.example.sonicflow.presentation.playlist.PlaylistsScreen
import com.example.sonicflow.ui.theme.SonicFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SonicFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        startDestination = if (preferencesManager.isLoggedIn) {
                            Screen.Library.route
                        } else {
                            Screen.SignIn.route
                        },
                        preferencesManager = preferencesManager
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    startDestination: String,
    preferencesManager: PreferencesManager
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    preferencesManager.isLoggedIn = true
                    navController.navigate(Screen.Library.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    preferencesManager.isLoggedIn = true
                    navController.navigate(Screen.Library.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onTrackClick = { track ->
                    navController.navigate(Screen.Player.createRoute(track.id))
                },
                onNavigateToPlaylists = {
                    navController.navigate(Screen.Playlists.route)
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("trackId") { type = NavType.StringType }
            )
        ) {
            PlayerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.StringType }
            )
        ) {
            PlaylistDetailScreen(
                onBackClick = { navController.popBackStack() },
                onTrackClick = { track ->
                    navController.navigate(Screen.Player.createRoute(track.id))
                }
            )
        }
    }
}