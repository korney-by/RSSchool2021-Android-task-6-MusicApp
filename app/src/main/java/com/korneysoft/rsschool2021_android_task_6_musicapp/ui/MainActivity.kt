package com.korneysoft.rsschool2021_android_task_6_musicapp.ui


import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.korneysoft.rsschool2021_android_task_6_musicapp.MyApplication
import com.korneysoft.rsschool2021_android_task_6_musicapp.databinding.ActivityMainBinding
import com.korneysoft.rsschool2021_android_task_6_musicapp.player.service.PlayerService
import com.korneysoft.rsschool2021_android_task_6_musicapp.utils.msecToTime
import com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel.MainViewModel
import com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel.PlayerControl
import javax.inject.Inject

private const val TAG = "T6-MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isSeekBarTrackingTouch = false

    @Inject
    lateinit var model: MainViewModel

    private var playerControl: PlayerControl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //    bindService(Intent(applicationContext, PlayerService::class.java), serviceConnection, BIND_AUTO_CREATE)

        // enable scrolling for textLog
        binding.textLog.movementMethod = ScrollingMovementMethod()

        if (model is PlayerControl) {
            playerControl = model
        }
        //setListenersPlayer()
        setListenersPlayerControl()

        setListenerSeekBar()
        registerObservers()
        prefatorySetSeekBarSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
//        playerServiceBinder = null
//        mediaController?.unregisterCallback(callback)
//        mediaController = null
//        unbindService(serviceConnection)
    }


    private fun registerObservers() {

        model.playerEventLiveData.observe(this,
            Observer {
                playerStateApply(it)
            })


        model.currentTrackLiveData.observe(this,
            Observer {
                toLog("Change track: $it")
                showTrackInfo()
            })

//        model.isPlayingChangedLiveData.observe(this,
//            Observer {
//                toLog("Change isPlaying state: $it")
//                //showButtonPlayPause(it)
//                setSeekBarDuration(model.getDuration())
//            })

        model.progressChangedLiveData.observe(this,
            Observer {
                //toLog("Change Progress state: $it")
                if (!isSeekBarTrackingTouch) {
                    showProgressBar(it)
                }
            })
    }

    private fun playerStateApply(@PlaybackStateCompat.State state: Int) {
        when (state) {
            PlaybackStateCompat.STATE_BUFFERING -> {
                toLog("Player state: STATE_BUFFERING ($state)")
            }
            PlaybackStateCompat.STATE_CONNECTING -> {
                toLog("Player state: STATE_CONNECTING ($state)")
            }
            PlaybackStateCompat.STATE_ERROR -> {
                toLog("Player state: STATE_ERROR ($state)")
            }
            PlaybackStateCompat.STATE_FAST_FORWARDING -> {
                toLog("Player state: STATE_FAST_FORWARDING ($state)")
            }
            PlaybackStateCompat.STATE_NONE -> {
                toLog("Player state: STATE_NONE ($state)")
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                toLog("Player state: STATE_PAUSED ($state)")
                showButtonPlay()
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                toLog("Player state: STATE_PLAYING ($state)")
                showButtonPause()
            }
            PlaybackStateCompat.STATE_REWINDING -> {
                toLog("Player state: STATE_REWINDING ($state)")
            }
            PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> {
                toLog("Player state: STATE_SKIPPING_TO_NEXT ($state)")
                showTrackInfo()
            }
            PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> {
                toLog("Player state: STATE_SKIPPING_TO_PREVIOUS ($state)")
                showTrackInfo()
            }
            PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM -> {
                toLog("Player state: STATE_SKIPPING_TO_QUEUE_ITEM ($state)")
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                toLog("Player state: STATE_STOPPED ($state)")
                showButtonPlay()
            }
        }
    }


    private fun setListenersPlayerControl() {
        binding.playerNext.setOnClickListener {
            toLog("Click Next button")
            playerControl?.next()
        }
        binding.playerPrevious.setOnClickListener {
            toLog("Click Previous button")
            playerControl?.previous()
        }
        binding.playerPlay.setOnClickListener {
            toLog("Click Play button")
            playerControl?.play()
        }
        binding.playerPause.setOnClickListener {
            toLog("Click Pause button")
            playerControl?.pause()
        }
        binding.playerStop.setOnClickListener {
            toLog("Click Stop button")
            playerControl?.stop()
        }
    }


//    private fun setListenersPlayer() {
//        binding.playerNext.setOnClickListener {
//            toLog("Click Next button")
//            if (model.nextTrack()) {
//                prefatorySetSeekBarSettings()
//            }
//        }
//        binding.playerPrevious.setOnClickListener {
//            toLog("Click Previous button")
//            if (model.previousTrack()) {
//                prefatorySetSeekBarSettings()
//            }
//        }
//        binding.playerPlay.setOnClickListener {
//            toLog("Click Play button")
//            model.playPlayer()
//        }
//        binding.playerPause.setOnClickListener {
//            toLog("Click Pause button")
//            model.pausePlayer()
//        }
//        binding.playerStop.setOnClickListener {
//            toLog("Click Stop button")
//            model.stopPlayer()
//        }
//    }

    private fun setListenerSeekBar() {
        binding.playSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                playSeekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                setSeekBarPosition(progress.toLong())
            }

            override fun onStartTrackingTouch(playSeekBar: SeekBar?) {
                isSeekBarTrackingTouch = true
            }

            override fun onStopTrackingTouch(playSeekBar: SeekBar?) {
                playSeekBar?.let {
                    //model.seekToPositionPlayer(it.progress.toLong())
                    isSeekBarTrackingTouch = false
                }
            }
        })
    }

    private fun showButtonPause() {
        binding.playerPlay.visibility = View.GONE
        binding.playerPause.visibility = View.VISIBLE
    }

    private fun showButtonPlay() {
        binding.playerPause.visibility = View.GONE
        binding.playerPlay.visibility = View.VISIBLE

    }

    private fun prefatorySetSeekBarSettings() {
        setSeekBarPosition(0)
        model.currentTrack?.let {
            setSeekBarDuration(it.duration)
        } ?: setSeekBarDuration(0)
    }

    private fun setSeekBarPosition(position: Long) {
        binding.playPosition.text = msecToTime(position.toLong())
    }

    private fun setSeekBarDuration(duration: Long) {
        var applyDuration = 0L
        if (duration > 0) {
            applyDuration = duration
        }
        binding.playSeekBar.max = applyDuration.toInt()

        //Log.d(TAG, "setSeekBarDuration - $applyDuration")
        binding.playDuration.text = msecToTime(applyDuration)
    }

    private fun showTrackInfo() {
        model.currentTrack?.let { track ->
            binding.trackTitle.text = track.title
            setSeekBarDuration(model.getDuration())
            showCover(track.bitmapUri)
        }
    }

    private fun showProgressBar(progress: Long) {
        val progressView = progress.toInt()
        Log.d(TAG, "showProgressBar MAPPING - $progressView")
        if (progressView != binding.playSeekBar.progress) {
            binding.playSeekBar.progress = progressView
        }
    }

    private fun toLog(event: String) {
        binding.textLog.append("$event\n")
    }

    private fun showCover(uri: String) {
        val view = binding.trackCover
        Glide.with(view.context)
            .load(uri)
            .centerCrop()
            //.error(R.drawable.ic_baseline_close_24)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    toLog("Image loading - Error")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    toLog("Image loading - Ok")
                    return false
                }
            })
            .into(view)
    }
}
