package com.korneysoft.rsschool2021_android_task_6_musicapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class TracksFromAssetsFile @Inject constructor(private val context: Context) : Tracks {

    private val list: List<Track>

    private val maxIndex: Int get() = list.size - 1
    private var currentIndex = 0

    override val current: Track
        get() = list[currentIndex]

    override val next: Track
        get() {
            currentIndex = if (currentIndex == maxIndex) 0 else currentIndex.plus(1)
            return current
        }

    override val previous: Track
        get(){
            currentIndex = if (currentIndex == 0) maxIndex else currentIndex.minus(1)
            return current
        }

    init {
        list = initListOfTrack()
    }

    private fun initListOfTrack(): List<Track> {
        val gson = Gson()
        val jsonString = readAsset("playlist.json")
        val listOfTrackType: Type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(jsonString, listOfTrackType)
    }

    private fun readAsset(fileName: String): String {
        try {
            return context
                .assets
                .open(fileName)
                .bufferedReader()
                .use(BufferedReader::readText)
        } catch (e: Exception) {
            return "[]"
        }
    }

}
