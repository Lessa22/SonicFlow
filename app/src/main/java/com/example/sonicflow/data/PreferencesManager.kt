package com.example.sonicflow.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sonicflow_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_TRACK_ID = "last_track_id"
        private const val KEY_LAST_POSITION = "last_position"
        private const val KEY_WAS_PLAYING = "was_playing"
    }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var lastTrackId: Long
        get() = prefs.getLong(KEY_LAST_TRACK_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_LAST_TRACK_ID, value).apply()

    var lastPosition: Long
        get() = prefs.getLong(KEY_LAST_POSITION, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_POSITION, value).apply()

    var wasPlaying: Boolean
        get() = prefs.getBoolean(KEY_WAS_PLAYING, false)
        set(value) = prefs.edit().putBoolean(KEY_WAS_PLAYING, value).apply()

    fun savePlayerState(trackId: Long, position: Long, isPlaying: Boolean) {
        prefs.edit().apply {
            putLong(KEY_LAST_TRACK_ID, trackId)
            putLong(KEY_LAST_POSITION, position)
            putBoolean(KEY_WAS_PLAYING, isPlaying)
            apply()
        }
    }
}