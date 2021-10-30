package com.korneysoft.rsschool2021_android_task_6_musicapp.player.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.MusicPlayer
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
class ServiceConnectionModule {

    @Provides
    fun provideServiceConnection(context: Context): ServiceConnectionController {
        return ServiceConnectionController(context)
    }
}
