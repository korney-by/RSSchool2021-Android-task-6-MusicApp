package com.korneysoft.rsschool2021_android_task_6_musicapp.di

import android.content.Context
import com.korneysoft.rsschool2021_android_task_6_musicapp.music_service.ServiceConnectionController
import dagger.Module
import dagger.Provides

@Module
class ServiceConnectionModule {
    @Provides
    fun provideServiceConnection(context: Context): ServiceConnectionController {
        return ServiceConnectionController(context)
    }
}
