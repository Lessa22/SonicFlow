package com.example.sonicflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sonicflow.MainActivity
import com.example.sonicflow.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    private var mediaSession: MediaSession? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "music_playback"
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NotificationManager::class.java)

        // 1. Créer le canal
        createNotificationChannel()

        // 2. Créer notification basique et startForeground
        val notification = createBasicNotification()
        startForeground(NOTIFICATION_ID, notification)

        // 3. Créer MediaSession
        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        // 4. Écouter les changements
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }

            override fun onMediaItemTransition(
                mediaItem: androidx.media3.common.MediaItem?,
                reason: Int
            ) {
                updateNotification()
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lecture musicale",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Contrôles de lecture audio"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createBasicNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SonicFlow")
            .setContentText("Lecteur audio")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val metadata = player.currentMediaItem?.mediaMetadata

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(metadata?.title ?: "SonicFlow")
            .setContentText(metadata?.artist ?: "Lecteur audio")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(player.isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // Ajouter l'artwork si disponible
        metadata?.artworkUri?.let { uri ->
            try {
                val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                }
                builder.setLargeIcon(bitmap)
            } catch (e: Exception) {
                // Ignorer
            }
        }

        return builder.build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}