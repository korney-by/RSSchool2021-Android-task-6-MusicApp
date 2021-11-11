package com.korneysoft.rsschool2021_android_task_6_musicapp.data

interface TrackRepository {
    val previous: Track
    val current: Track
    val next: Track
}
