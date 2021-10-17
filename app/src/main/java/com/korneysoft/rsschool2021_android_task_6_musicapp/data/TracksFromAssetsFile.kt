package com.korneysoft.rsschool2021_android_task_6_musicapp.data

import android.content.Context
import com.google.gson.Gson
import java.io.BufferedReader
import javax.inject.Inject as Inject
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Singleton

@Singleton
class TracksFromAssetsFile @Inject constructor(private val context: Context) : Tracks {

    override val list: List<Track>

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
        val result: String = try {
            return context
                .assets
                .open(fileName)
                .bufferedReader()
                .use(BufferedReader::readText)
        } catch (e: Exception) {
            return "[]"
        }
        return result
    }

}
