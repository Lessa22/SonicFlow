package com.example.sonicflow.presentation

sealed class Screen(val route: String) {
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object Library : Screen("library")
    object Player : Screen("player/{trackId}") {
        fun createRoute(trackId: Long) = "player/$trackId"
    }
    object Playlists : Screen("playlists")
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}