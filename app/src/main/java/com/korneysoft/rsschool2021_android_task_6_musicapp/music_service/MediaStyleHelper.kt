package com.korneysoft.rsschool2021_android_task_6_musicapp.music_service

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.korneysoft.rsschool2021_android_task_6_musicapp.R

// https://gist.github.com/ianhanniballake/47617ec3488e0257325c

/**
 * Helper APIs for constructing MediaStyle notifications
 */
internal object MediaStyleHelper {
    /**
     * Build a notification using the information from the given media session. Makes heavy use
     * of [MediaMetadataCompat.getDescription] to extract the appropriate information.
     *
     * @param context Context used to construct the notification.
     * @param mediaSession Media session to get information.
     * @return A pre-built notification with information from the given media session.
     */
    fun from(
        context: Context,
        mediaSession: MediaSessionCompat,
        channelId: String
    ): NotificationCompat.Builder {
        val controller = mediaSession.controller
        val mediaMetadata = controller.metadata
        val description = mediaMetadata.description
        return NotificationCompat.Builder(context, channelId).apply {
            setContentTitle(description.title)
            setContentText(description.subtitle)
            setSubText(description.description)
            setLargeIcon(description.iconBitmap)
            setContentIntent(controller.sessionActivity)
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            setSilent(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSmallIcon(R.drawable.ic_notification)
        }
    }
}
