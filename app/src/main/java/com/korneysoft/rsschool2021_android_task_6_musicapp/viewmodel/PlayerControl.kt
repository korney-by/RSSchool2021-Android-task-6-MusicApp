package com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel

interface PlayerControl {
    fun next()
    fun previous()
    fun play()
    fun pause()
    fun stop()
    fun seekTo(position:Long)
}