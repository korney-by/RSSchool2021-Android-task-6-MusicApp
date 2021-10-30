package com.korneysoft.rsschool2021_android_task_6_musicapp.player.service

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.cache.*
import com.korneysoft.rsschool2021_android_task_6_musicapp.MyApplication
import com.korneysoft.rsschool2021_android_task_6_musicapp.R
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Tracks
import com.korneysoft.rsschool2021_android_task_6_musicapp.ui.MainActivity
import kotlinx.coroutines.internal.synchronized
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "PlayerService"

class PlayerService() : Service() { //MediaBrowserServiceCompat
    @Inject
    lateinit var tracks: Tracks

    private val metadataBuilder = MediaMetadataCompat.Builder()
    private val stateBuilder = PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        //or PlaybackStateCompat.ACTION_SEEK_TO
    )
    private lateinit var audioManager: AudioManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var exoPlayer: SimpleExoPlayer

    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusRequested = false

    override fun onCreate() {
        (application as MyApplication).appComponent.inject(this)

        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(
                NOTIFICATION_DEFAULT_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(audioAttributes)
                .build()
        }

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        val appContext = applicationContext
        val activityIntent = Intent(appContext, MainActivity::class.java)
        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            }
            setCallback(mediaSessionCallback)
            setSessionActivity(
                PendingIntent.getActivity(
                    appContext,
                    0, // TODO setup Request код ?
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }

        val mediaButtonIntent = Intent(
            Intent.ACTION_MEDIA_BUTTON, null, appContext,
            MediaButtonReceiver::class.java
        )
        mediaSession.setMediaButtonReceiver(
            PendingIntent.getBroadcast(
                appContext,
                0,
                mediaButtonIntent,
                0
            )
        )

        @ExtensionRendererMode
        val extensionRendererMode = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
        val renderersFactory =
            DefaultRenderersFactory(this).setExtensionRendererMode(extensionRendererMode);

        exoPlayer = SimpleExoPlayer.Builder(this, renderersFactory)
            .setTrackSelector(DefaultTrackSelector(this))
            .setLoadControl(DefaultLoadControl.Builder().build())
            .build()

        exoPlayer.addListener(exoPlayerListener)


//        val httpDataSourceFactory: DataSource.Factory = OkHttpDataSourceFactory(
//            OkHttpClient(),
//            Util.getUserAgent(this, getString(R.string.app_name))
//        )
//        val cache: Cache = SimpleCache(
//            File(this.cacheDir.absolutePath + "/exoplayer"),
//            LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100)
//        ) // 100 Mb max
//        dataSourceFactory = CacheDataSourceFactory(
//            cache,
//            httpDataSourceFactory,
//            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
//        )
//        extractorsFactory = DefaultExtractorsFactory()
//    }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        exoPlayer.release()
    }

    private val mediaSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            private var currentUri: String? = null
            var currentState = PlaybackStateCompat.STATE_STOPPED

            override fun onPlay() {
                if (!exoPlayer.playWhenReady) {
                    startService(Intent(applicationContext, PlayerService::class.java))
                    val track = tracks.current

                    updateMetadataFromTrack(track)
                    prepareToPlay(track.trackUri)

                    if (!audioFocusRequested) {
                        audioFocusRequested = true

                        var audioFocusResult: Int = AudioManager.AUDIOFOCUS_REQUEST_FAILED
                        @Suppress("DEPRECATION")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            audioFocusRequest?.let { audioFocusRequest ->
                                audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest)
                            }
                        } else {
                            audioFocusResult = audioManager.requestAudioFocus(
                                audioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN
                            )
                        }
                        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
                    }

                    // Указываем, что наше приложение теперь активный плеер и кнопки
                    // на окне блокировки должны управлять именно нами
                    mediaSession.isActive = true // Сразу после получения фокуса

                    registerReceiver(
                        becomingNoisyReceiver,
                        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                    )
                    exoPlayer.playWhenReady = true
                }

                // Сообщаем новое состояние
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        exoPlayer.currentPosition,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PLAYING
                refreshNotificationAndForegroundStatus(currentState)
            }

            override fun onPause() {
                if (exoPlayer.playWhenReady) {
                    exoPlayer.playWhenReady = false
                    unregisterReceiver(becomingNoisyReceiver)
                }
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        exoPlayer.currentPosition,
                        0f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PAUSED
                refreshNotificationAndForegroundStatus(currentState)
            }

            override fun onStop() {
                if (exoPlayer.playWhenReady) {
                    exoPlayer.playWhenReady = false
                    unregisterReceiver(becomingNoisyReceiver)
                }

                if (audioFocusRequested) {
                    audioFocusRequested = false
                    @Suppress("DEPRECATION")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioFocusRequest?.let { audioFocusRequest ->
                            audioManager.abandonAudioFocusRequest(audioFocusRequest)
                        }
                    } else {
                        audioManager.abandonAudioFocus(audioFocusChangeListener)
                    }
                }
                mediaSession.isActive = false
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        0f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_STOPPED
                refreshNotificationAndForegroundStatus(currentState)
                stopSelf()
            }

            override fun onSkipToNext() {
                val track = tracks.next
                updateMetadataFromTrack(track)
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        currentState,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                refreshNotificationAndForegroundStatus(currentState)
                prepareToPlay(track.trackUri)
            }

            override fun onSkipToPrevious() {
                val track = tracks.previous
                updateMetadataFromTrack(track)
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        currentState,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                refreshNotificationAndForegroundStatus(currentState)
                prepareToPlay(track.trackUri)
            }

            private fun prepareToPlay(uri: String) {
                if (uri != currentUri) {
                    currentUri = uri
                    val mediaItem = MediaItem.fromUri(uri)

                    exoPlayer.run {
                        setMediaItem(mediaItem)
                        prepare()
                    }
                }
            }

            private fun updateMetadataFromTrack(track: Track) {
                asyncLoadBitmap(track.bitmapUri)
                metadataBuilder.apply {
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.artist)
                    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
                    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.duration)
                    mediaSession.setMetadata(this.build())
                }

            }

            private fun asyncLoadBitmap(uri: String) {
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(uri)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            if (tracks.current.bitmapUri == uri) {
                                metadataBuilder.apply {
                                    putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resource)
                                    mediaSession.setMetadata(this.build())
                                }
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }

    private val audioFocusChangeListener: OnAudioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> mediaSessionCallback.onPlay() // Не очень красиво
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaSessionCallback.onPause()
                else -> mediaSessionCallback.onPause()
            }
        }
    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Disconnecting headphones - stop playback
            if ((AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action)) {
                mediaSessionCallback.onPause()
            }
        }
    }

    private val exoPlayerListener: Player.Listener = object : Player.Listener {
        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
        }

        override fun onLoadingChanged(isLoading: Boolean) {}

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playWhenReady && playbackState == ExoPlayer.STATE_ENDED) {
                mediaSessionCallback.onSkipToNext()
            }
        }

        fun onPlayerError(error: ExoPlaybackException?) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    }

    override fun onBind(intent: Intent): IBinder {
        return PlayerServiceBinder()
    }


    inner class PlayerServiceBinder() : Binder() {
        val mediaSessionToken: MediaSessionCompat.Token
            get() = mediaSession.sessionToken
    }

    private fun refreshNotificationAndForegroundStatus(playbackState: Int) {
        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> {
                startForeground(NOTIFICATION_ID, getNotification(playbackState))
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                NotificationManagerCompat.from(this@PlayerService)
                    .notify(NOTIFICATION_ID, getNotification(playbackState))
                stopForeground(false)
            }
            else -> {
                stopForeground(true)
            }
        }
    }

    private fun getNotification(playbackState: Int): Notification {
        val builder: NotificationCompat.Builder =
            MediaStyleHelper.from(this, mediaSession, NOTIFICATION_DEFAULT_CHANNEL_ID)
        builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_previous,
                getString(R.string.previous_button),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
        )
        if (playbackState == PlaybackStateCompat.STATE_PLAYING) builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                getString(R.string.pause_button),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        ) else builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                getString(R.string.play_button),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        )
        builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_next,
                getString(R.string.next_button),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )
        )
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
                .setMediaSession(mediaSession.sessionToken)
        ) // setMediaSession требуется для Android Wear
        builder.setSmallIcon(R.mipmap.ic_launcher)

        // The whole background (in MediaStyle), not just icon background
        builder.color = ContextCompat.getColor(this, R.color.design_default_color_primary_dark)

        builder.setShowWhen(false)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setOnlyAlertOnce(true)
//        builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID)
        return builder.build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel"

    }
}
