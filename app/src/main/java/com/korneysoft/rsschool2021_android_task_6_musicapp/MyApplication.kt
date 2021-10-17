package com.korneysoft.rsschool2021_android_task_6_musicapp

import android.app.Application
import com.korneysoft.rsschool2021_android_task_6_musicapp.di.AppComponent
import com.korneysoft.rsschool2021_android_task_6_musicapp.di.DaggerAppComponent

class MyApplication:Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }
}