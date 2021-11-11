package com.korneysoft.rsschool2021_android_task_6_musicapp.di

import android.content.Context
import com.korneysoft.rsschool2021_android_task_6_musicapp.music_service.ServiceConnectionController
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class ServiceConnectionModule {
    @Singleton
    @Provides
    fun provideServiceConnection(context: Context): ServiceConnectionController {
        return ServiceConnectionController(context)
    }
}

