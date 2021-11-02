package com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.music_service.ServiceConnectionController
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "T6-MainViewModel"

@Singleton
class MainViewModel @Inject constructor() : ViewModel(), PlayerControl {

    @Inject
    lateinit var serviceConnectionController: ServiceConnectionController

    private val mediaController by lazy {
        serviceConnectionController.mediaController
    }
    val currentTrackLiveData: LiveData<Track?> by lazy {
        serviceConnectionController.currentTrackLiveData
    }
    val playerEventLiveData: LiveData<Int> by lazy {
        serviceConnectionController.eventLiveData
    }
    val playbackPositionLiveData: LiveData<Long> by lazy {
        serviceConnectionController.playbackPositionLiveData
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
