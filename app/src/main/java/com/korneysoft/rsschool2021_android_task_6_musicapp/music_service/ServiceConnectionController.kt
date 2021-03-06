package com.korneysoft.rsschool2021_android_task_6_musicapp.music_service

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
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel.PlayerCommand
import javax.inject.Inject

class ServiceConnectionController @Inject constructor(private val context: Context) {

    private val _eventLiveData = MutableLiveData(PlaybackStateCompat.STATE_NONE)
    val eventLiveData: LiveData<Int?> = _eventLiveData

    private val _trackPlaybackPositionLiveData: MutableLiveData<Long?> = MutableLiveData(null)
    val playbackPositionLiveData: LiveData<Long?> = _trackPlaybackPositionLiveData

    private val _currentTrackLiveData: MutableLiveData<Track?> = MutableLiveData(null)
    val currentTrackLiveData: LiveData<Track?> = _currentTrackLiveData

    private var mediaController: MediaControllerCompat? = null

    private var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    private val callback by lazy { createCallbackService() }
    private val serviceConnection by lazy { createServiceConnection() }

    init {
        context.bindService(
            Intent(context, PlayerService()::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    private fun createServiceConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder

                playerServiceBinder?.let { playerServiceBinder ->
                    playerServiceBinder.setCallbackPlaybackPosition { position ->
                        onChangePosition(position)
                    }
                    playerServiceBinder.setCallbackCurrentTrackChanged { track ->
                        onChangeCurrentTrack(track)
                    }

                    mediaController = try {
                        MediaControllerCompat(
                            context,
                            playerServiceBinder.mediaSessionToken
                        ).apply {
                            registerCallback(callback)
                            callback.onPlaybackStateChanged(playbackState)
                        }
                    } catch (e: RemoteException) {
                        null
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                playerServiceBinder = null
                mediaController?.unregisterCallback(callback)
                mediaController = null
                context.unbindService(serviceConnection)
            }
        }
    }

    private fun createCallbackService(): MediaControllerCompat.Callback {
        return object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)
                state ?: return
                if (_eventLiveData.value != state.state) {
                    _eventLiveData.value = state.state
                }
            }
        }
    }

    private fun onChangePosition(position: Long) {
        _trackPlaybackPositionLiveData.value = position
    }

    private fun onChangeCurrentTrack(track: Track) {
        _currentTrackLiveData.value = track
    }

    fun executeCommandPlayer(command: PlayerCommand) {
        mediaController.apply {
            when (command) {
                PlayerCommand.NEXT -> mediaController?.transportControls?.skipToNext()
                PlayerCommand.PREVIOUS -> mediaController?.transportControls?.skipToPrevious()
                PlayerCommand.PLAY -> {
                    if (_eventLiveData.value != PlaybackStateCompat.STATE_PLAYING) {
                        mediaController?.transportControls?.play()
                    }
                }
                PlayerCommand.PAUSE -> mediaController?.transportControls?.pause()
                PlayerCommand.STOP -> mediaController?.transportControls?.stop()
                PlayerCommand.NONE -> Unit
            }
        }
    }

    fun seekTo(position: Long) {
        mediaController?.transportControls?.seekTo(position)
    }
}
