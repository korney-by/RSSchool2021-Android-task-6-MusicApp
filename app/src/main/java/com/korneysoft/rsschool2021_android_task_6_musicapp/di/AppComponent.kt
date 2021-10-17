package com.korneysoft.rsschool2021_android_task_6_musicapp.di

import com.korneysoft.rsschool2021_android_task_6_musicapp.MainActivity
import android.content.Context

import dagger.*
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(activity: MainActivity)
}
