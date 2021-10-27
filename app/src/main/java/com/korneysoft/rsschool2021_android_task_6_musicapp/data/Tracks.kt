package com.korneysoft.rsschool2021_android_task_6_musicapp.data

interface Tracks {
        val list: List<Track>
    val previous: Track
    val current: Track
    val next: Track
}
