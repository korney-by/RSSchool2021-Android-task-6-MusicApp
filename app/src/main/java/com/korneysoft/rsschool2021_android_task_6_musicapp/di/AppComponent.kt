package com.korneysoft.rsschool2021_android_task_6_musicapp.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.korneysoft.rsschool2021_android_task_6_musicapp.MainActivity
import com.korneysoft.rsschool2021_android_task_6_musicapp.R
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Tracks
import dagger.Component
import dagger.Module
import dagger.Provides


@Component(modules = [DataModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    val tracks: Tracks
}

@Module
object DataModule {

    @Provides
    fun provideTracks(): List<Track> {
        val gson = Gson()
        val jsonFile: String = ApplicationContext.assets.open("ela.json").bufferedReader().use {it.readText()}
        return gson.fromJson(jsonFile, Tracks.ListOfTrack::class.java)

    }

    @Provides
    fun providerGson(): Gson? {
        val gsonBuilder = GsonBuilder()
        return gsonBuilder.create()
    }


}
