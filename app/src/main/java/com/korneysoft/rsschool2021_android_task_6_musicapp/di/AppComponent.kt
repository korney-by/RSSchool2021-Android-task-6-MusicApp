package com.korneysoft.rsschool2021_android_task_6_musicapp.di

import com.korneysoft.rsschool2021_android_task_6_musicapp.MainActivity
import dagger.Component
import dagger.Module
import dagger.Provides
import android.app.Application
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Tracks

import javax.inject.Singleton




@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    val tracks: Tracks
}

@Module
object DataModule {

    @Provides
    fun provideTracks(

    )

}
