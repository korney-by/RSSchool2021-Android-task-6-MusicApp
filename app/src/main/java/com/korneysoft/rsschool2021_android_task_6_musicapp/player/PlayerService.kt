package com.korneysoft.rsschool2021_android_task_6_musicapp.player

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
private const val TAG="PlayerService"

@Singleton
class PlayerService @Inject constructor(private val context: Context): Service() {
    override fun onCreate() {
        Log.d(TAG,"onCreate")
    }

    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun initializePlayer() {
//        val trackSelector = DefaultTrackSelector(this).apply {
//            setParameters(buildUponParameters().setMaxVideoSizeSd())
//        }
//        player = SimpleExoPlayer.Builder(this)
//            .setTrackSelector(trackSelector)
//            .build()
//            .also { exoPlayer ->
//                viewBinding.videoView.player = exoPlayer
//
//                //val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
//                val mediaItem = MediaItem.Builder()
//                    .setUri(getString(R.string.media_url_dash))
//                    .setMimeType(MimeTypes.APPLICATION_MPD)
//                    .build()
//
//                exoPlayer.setMediaItem(mediaItem)
//
//                exoPlayer.seekTo(currentWindow, playbackPosition)
//                exoPlayer.addListener(playbackStateListener)
//
//                exoPlayer.playWhenReady = playWhenReady
//                exoPlayer.seekTo(currentWindow, playbackPosition)
//                exoPlayer.prepare()
    }

}