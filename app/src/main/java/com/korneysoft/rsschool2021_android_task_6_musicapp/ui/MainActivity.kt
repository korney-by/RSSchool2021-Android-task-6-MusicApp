package com.korneysoft.rsschool2021_android_task_6_musicapp.ui



import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.korneysoft.rsschool2021_android_task_6_musicapp.MyApplication
import com.korneysoft.rsschool2021_android_task_6_musicapp.R
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
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // enable scrolling for textLog
        binding.textLog.movementMethod = ScrollingMovementMethod()

        setListeners()
        registerObserverNumTrack()
    }

    private fun registerObserverNumTrack() {
        model.currentTrackLiveData.observe(this,
            Observer {
                toLog("LiveData observer: $it")
                showInfo()
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
