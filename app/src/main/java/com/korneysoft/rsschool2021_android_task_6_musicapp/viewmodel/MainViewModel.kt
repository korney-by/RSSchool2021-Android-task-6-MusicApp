package com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Tracks
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var tracks: Tracks

    private var currentTrackNum = 0
        set(newValue) {
            _currentTrackLiveData.value = newValue
            field = newValue
        }

    private val _currentTrackLiveData = MutableLiveData(currentTrackNum)
    val currentTrackLiveData: LiveData<Int> get() = _currentTrackLiveData

    val currentTrack: Track?
        get() {
            return if (currentTrackNum in 0..tracks.list.size - 1) tracks.list[currentTrackNum]
            else null
        }

    fun nextTrack() {
        if (currentTrackNum < tracks.list.size - 1) {
            currentTrackNum += 1
        }
    }

    fun previousTrack() {
        if (currentTrackNum > 0) {
            currentTrackNum -= 1
        }
    }


}
