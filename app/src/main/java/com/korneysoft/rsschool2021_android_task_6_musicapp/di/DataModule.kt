package com.korneysoft.rsschool2021_android_task_6_musicapp.di

import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Tracks
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.TracksFromAssetsFile
import dagger.Binds
import dagger.Module

@Module
abstract class DataModule {
    @Binds
    abstract fun provideTracks(tracks: TracksFromAssetsFile): Tracks
}
