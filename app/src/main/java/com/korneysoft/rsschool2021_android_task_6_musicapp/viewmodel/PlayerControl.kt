package com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel

import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.MusicPlayer
import javax.inject.Inject

interface PlayerControl {
    fun next()
    fun previous()
    fun play()
    fun pause()
    fun stop()
}