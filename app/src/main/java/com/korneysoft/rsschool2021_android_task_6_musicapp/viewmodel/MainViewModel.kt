package com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel

//import androidx.test.core.app.ApplicationProvider.getApplicationContext

import android.content.Intent
import android.content.ServiceConnection
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Tracks
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.MusicPlayer
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.service.PlayerService
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.service.ServiceConnectionController
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "T6-MainViewModel"

@Singleton
class MainViewModel @Inject constructor() : ViewModel(), PlayerControl {

    @Inject
    lateinit var tracks: Tracks
//
//    @Inject
//    lateinit var musicPlayer: MusicPlayer

    @Inject
    lateinit var serviceConnectionController: ServiceConnectionController

    private val mediaController by lazy { serviceConnectionController.mediaController }
    val playerEventLiveData: LiveData<Int>
        get() {
            return if (this::serviceConnectionController.isInitialized){
                serviceConnectionController.eventLiveData
            }else{
                MutableLiveData(0)
            }
        }

    private var currentTrackNum = 0
        set(newValue) {
            if (field != newValue) {
                field = newValue
                _currentTrackLiveData.value = newValue
            }
        }

    private val _currentTrackLiveData = MutableLiveData(currentTrackNum)
    val currentTrackLiveData: LiveData<Int> get() = _currentTrackLiveData

    private val _isPlayingChangedLiveData = MutableLiveData(false)
    val isPlayingChangedLiveData: LiveData<Boolean> get() = _isPlayingChangedLiveData

    private val _progressChangedLiveData = MutableLiveData(0.toLong())
    val progressChangedLiveData: LiveData<Long> get() = _progressChangedLiveData

    val currentTrack: Track?
        get() {
            return tracks.current
//            musicPlayer.setCallbackPlayingStateChanged { onIsPlayingStateChanged() }
//            return if (currentTrackNum in tracks.list.indices) tracks.list[currentTrackNum]
//            else null
        }

//    private fun onIsPlayingStateChanged(value: Boolean? = null) {
//        val isPlayingValue = value?.let {
//            it
//        } ?: run {
//            musicPlayer.isPlaying
//        }
//        _isPlayingChangedLiveData.value = isPlayingValue
//        if (isPlayingValue) {
//            musicPlayer.setCallbackProgressChanged { position: Long -> onProgressChanged(position) }
//        }
//    }

    private fun onProgressChanged(position: Long) {
        _progressChangedLiveData.value = position
        // Log.d(TAG, "Position: ${position}/${musicPlayer.duration}  -  $viewPosition")
    }

//    fun nextTrack(): Boolean {
//        if (currentTrackNum < tracks.list.size - 1) {
//            currentTrackNum += 1
//            if (musicPlayer.isPlaying == true) playPlayer()
//            return true
//        }
//        return false
//    }

//    fun previousTrack(): Boolean {
//        if (currentTrackNum > 0) {
//            currentTrackNum -= 1
//            if (musicPlayer.isPlaying) playPlayer()
//            return true
//        }
//        return false
//    }

//    fun playPlayer() {
//        onIsPlayingStateChanged(true)
//        currentTrack?.let { track ->
//            musicPlayer.play(track.trackUri)
//        }
//    }

//    fun pausePlayer() {
//        onIsPlayingStateChanged(false)
//        musicPlayer.pause()
//    }
//
//    fun stopPlayer() {
//        musicPlayer.stop()
//    }
//
//    fun seekToPositionPlayer(position: Long) {
//        musicPlayer.seekToPosition(position)
//    }

    fun getDuration(): Long {
        currentTrack?.let {
            return it.duration
//            if (it.duration > 0 && musicPlayer.duration <= 0) {
//                return it.duration
//            }
//            if (musicPlayer.duration > 0) {
//                return musicPlayer.duration
//            }
        }
        return 0
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
