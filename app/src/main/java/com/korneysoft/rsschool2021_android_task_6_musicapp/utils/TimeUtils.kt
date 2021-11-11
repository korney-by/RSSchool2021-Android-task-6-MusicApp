package com.korneysoft.rsschool2021_android_task_6_musicapp.utils

import java.util.concurrent.TimeUnit

fun msecToTime(milliseconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds))

    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))

    return if (hours > 0) {
        String.format("%01d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%01d:%02d", minutes, seconds)
    }
}
