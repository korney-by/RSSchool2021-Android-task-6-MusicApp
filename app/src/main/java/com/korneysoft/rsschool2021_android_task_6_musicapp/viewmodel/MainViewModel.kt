package com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Tracks
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.service.ServiceConnectionController
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "T6-MainViewModel"

@Singleton
class MainViewModel @Inject constructor() : ViewModel(), PlayerControl {

    @Inject
    lateinit var serviceConnectionController: ServiceConnectionController

    private val mediaController by lazy { serviceConnectionController.mediaController }

    val currentTrackLiveData: LiveData<Track>
        get() {
            return serviceConnectionController.currentTrackLiveData
            return if (this::serviceConnectionController.isInitialized) {
                serviceConnectionController.currentTrackLiveData
            } else {
                MutableLiveData(null)
            }
        }

    val playerEventLiveData: LiveData<Int>
        get() {
            return serviceConnectionController.eventLiveData
            return if (this::serviceConnectionController.isInitialized) {
                serviceConnectionController.eventLiveData
            } else {
                MutableLiveData(0)
            }
        }

    val playbackPositionLiveData: LiveData<Long>
        get() {
            return serviceConnectionController.playbackPositionLiveData
            return if (this::serviceConnectionController.isInitialized) {
                serviceConnectionController.playbackPositionLiveData
            } else {
                MutableLiveData(0L)
            }
        }

    override fun seekTo(position: Long) {
        mediaController?.transportControls?.seekTo(position)
    }

    override fun next() {
        Log.d(TAG, "next")
        mediaController?.transportControls?.skipToNext()
    }

    override fun previous() {
        Log.d(TAG, "previous")
        mediaController?.transportControls?.skipToPrevious()
    }

    override fun play() {
        Log.d(TAG, "play")
        mediaController?.transportControls?.play()
    }

    override fun pause() {
        Log.d(TAG, "pause")
        mediaController?.transportControls?.pause()
    }

    override fun stop() {
        Log.d(TAG, "stop")
        mediaController?.transportControls?.stop()
    }

}
