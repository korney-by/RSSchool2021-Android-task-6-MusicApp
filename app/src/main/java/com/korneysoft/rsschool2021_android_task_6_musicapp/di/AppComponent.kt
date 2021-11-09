package com.korneysoft.rsschool2021_android_task_6_musicapp.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.korneysoft.rsschool2021_android_task_6_musicapp.music_service.PlayerService
import com.korneysoft.rsschool2021_android_task_6_musicapp.ui.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class, ServiceConnectionModule::class])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(activity: MainActivity)
    fun inject(service: PlayerService)
}
