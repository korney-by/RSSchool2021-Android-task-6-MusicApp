package com.korneysoft.rsschool2021_android_task_6_musicapp.data

object tracks{
    val list: List<Track> = TODO()
}

data class Track(
    val title: String = "",
    val artist: String = "",
    val bitmapUri: String = "",
    val trackUri: String = "",
    val duration: Long = 0
)
