package com.korneysoft.rsschool2021_android_task_6_musicapp.data

import com.google.gson.Gson
import com.korneysoft.rsschool2021_android_task_6_musicapp.R

class Tracks {

    data class ListOfTrack(
        val list: List<Track>
    )

    data class Track(
        val title: String = "",
        val artist: String = "",
        val bitmapUri: String = "",
        val trackUri: String = "",
        val duration: Long = 0
    )
}

fun main() {
    val gson = Gson()
    val mTracks = gson.fromJson(R.asset., Tracks.ListOfTrack::class.java)
}
