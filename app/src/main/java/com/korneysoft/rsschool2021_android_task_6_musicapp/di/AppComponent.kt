package com.korneysoft.rsschool2021_android_task_6_musicapp.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.service.PlayerService
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.service.ServiceConnectionModule

import com.korneysoft.rsschool2021_android_task_6_musicapp.ui.MainActivity

import dagger.*
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class,  ServiceConnectionModule::class]) // PlayerModule::class
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(activity: MainActivity)
    fun inject(viewModel: ViewModel)
    fun inject(service: PlayerService)
}
