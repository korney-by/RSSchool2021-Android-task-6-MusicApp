package com.korneysoft.rsschool2021_android_task_6_musicapp.application

import android.app.Application
import com.korneysoft.rsschool2021_android_task_6_musicapp.di.AppComponent

class App : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        this.appComponent = this.initDagger()
    }

    private fun initDagger() = DaggerAppComponent.builder().appModule(AppModule(this)).build()
}
