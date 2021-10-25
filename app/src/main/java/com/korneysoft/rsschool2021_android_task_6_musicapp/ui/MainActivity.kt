package com.korneysoft.rsschool2021_android_task_6_musicapp.ui



import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.korneysoft.rsschool2021_android_task_6_musicapp.MyApplication
import com.korneysoft.rsschool2021_android_task_6_musicapp.databinding.ActivityMainBinding
import com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel.MainViewModel
import javax.inject.Inject

private const val TAG = "T6-MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // enable scrolling for textLog
        binding.textLog.movementMethod = ScrollingMovementMethod()

        setListeners()
        registerObservers()

    }

    private fun registerObservers() {
        model.currentTrackLiveData.observe(this,
            Observer {
                toLog("Change track: $it")
                showInfo()
            })

        model.isPlayingChangedLiveData.observe(this,
            Observer {
                toLog("Change isPlaying state: $it")
                showButtonPlayPause(it)
            })
    }

    private fun setListeners() {
        binding.playerNext.setOnClickListener {
            toLog("Click Next button")
            model.nextTrack()
        }
        binding.playerPrevious.setOnClickListener {
            toLog("Click Previous button")
            model.previousTrack()
        }
        binding.playerPlay.setOnClickListener {
            toLog("Click Play button")
            model.playPlayer()
        }
        binding.playerPause.setOnClickListener {
            toLog("Click Pause button")
            model.pausePlayer()
        }
        binding.playerStop.setOnClickListener {
            toLog("Click Stop button")
            model.stopPlayer()
        }
    }

    private fun showButtonPlayPause(isPlaying:Boolean){
        if (isPlaying){
            binding.playerPlay.visibility= View.GONE
            binding.playerPause.visibility= View.VISIBLE
        }else{
            binding.playerPlay.visibility= View.VISIBLE
            binding.playerPause.visibility= View.GONE
        }
    }

    private fun showInfo() {
        model.currentTrack?.let { track ->
            binding.trackTitle.text = track.title
            showCover(track.bitmapUri)
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
