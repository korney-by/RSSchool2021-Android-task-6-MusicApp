package com.korneysoft.rsschool2021_android_task_6_musicapp.player.service

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceConnectionController @Inject constructor(private val context: Context) {

    private val _eventLiveData = MutableLiveData(PlaybackStateCompat.STATE_NONE)
    val eventLiveData: LiveData<Int> = _eventLiveData
    var mediaController: MediaControllerCompat? = null
        private set

    private var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    private val callback by lazy { createCallbackService() }
    private val connection by lazy { createServiceConnection() }

    init {
        context.bindService(
            Intent(context, PlayerService()::class.java),
            connection,
            BIND_AUTO_CREATE
        )
    }

    private fun createServiceConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                playerServiceBinder?.let { playerServiceBinder ->
                    try {
                        mediaController = MediaControllerCompat(
                            context,
                            playerServiceBinder.mediaSessionToken
                        )
                        mediaController?.let { mediaController ->
                            mediaController.registerCallback(callback)
                            callback.onPlaybackStateChanged(mediaController.playbackState)
                        }

                    } catch (e: RemoteException) {
                        mediaController = null
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                playerServiceBinder = null
                mediaController?.unregisterCallback(callback)
                mediaController = null
            }
        }
    }

    private fun createCallbackService(): MediaControllerCompat.Callback {
        return object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                state?: return
                _eventLiveData.value = state.state
            }
        }
    }
}
