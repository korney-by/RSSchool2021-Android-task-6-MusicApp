package com.korneysoft.rsschool2021_android_task_6_musicapp.player

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.cache.*
import com.korneysoft.rsschool2021_android_task_6_musicapp.R
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Track
import com.korneysoft.rsschool2021_android_task_6_musicapp.data.Tracks
import com.korneysoft.rsschool2021_android_task_6_musicapp.ui.MainActivity
import javax.inject.Inject


private const val TAG = "PlayerService"

//@Singleton
//class PlayerService @Inject constructor(private val context: Context): Service() {


//import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
//import com.google.android.exoplayer2.source.ExtractorMediaSource
//import okhttp3.OkHttpClient

class PlayerService() : Service() {
    @Inject
    lateinit var tracks: Tracks
    //private val musicRepository: MusicRepository = MusicRepository()

    private val NOTIFICATION_ID = 404
    private val NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel"
    private val metadataBuilder = MediaMetadataCompat.Builder()
    private val stateBuilder = PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    )
    private var mediaSession: MediaSessionCompat? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusRequested = false
    private var exoPlayer: SimpleExoPlayer? = null
    private var extractorsFactory: ExtractorsFactory? = null
    private var dataSourceFactory: DataSource.Factory? = null

    override fun onCreate() {
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
        mediaSession = MediaSessionCompat(this, "PlayerService").apply {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            }
            setCallback(mediaSessionCallback)
        }
        val appContext = applicationContext
        val activityIntent = Intent(appContext, MainActivity::class.java)
        mediaSession?.apply {
            setSessionActivity(
                PendingIntent.getActivity(
                    appContext,
                    0, // TODO setup Request код ?
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
// this is everything for API < 21
//        val mediaButtonIntent = Intent(
//            Intent.ACTION_MEDIA_BUTTON, null, appContext,
//            MediaButtonReceiver::class.java
//        )
//        mediaSession!!.setMediaButtonReceiver(
//            PendingIntent.getBroadcast(
//                appContext,
//                0,
//                mediaButtonIntent,
//                0
//            )
//        )
//
//        exoPlayer = ExoPlayerFactory.newSimpleInstance(
//
//            this,
//            DefaultRenderersFactory(this),
//            DefaultTrackSelector(),
//            DefaultLoadControl()
//        )
//      exoPlayer!!.addListener(exoPlayerListener)

        exoPlayer = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                exoPlayer.playWhenReady = true
                exoPlayer.addListener(exoPlayerListener)
                exoPlayer.prepare()

            }

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
        mediaSession?.release()
        exoPlayer?.release()
    }

    private val mediaSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            private var currentUri: String? = null
            var currentState = PlaybackStateCompat.STATE_STOPPED

            override fun onPlay() {
                if (!exoPlayer!!.playWhenReady) {
                    startService(Intent(applicationContext, PlayerService::class.java))
                    val track = tracks.current

                    updateMetadataFromTrack(track)
                    prepareToPlay(track.trackUri)
                    if (!audioFocusRequested) {
                        audioFocusRequested = true
                        val audioFocusResult: Int
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            audioFocusResult =
                                audioManager!!.requestAudioFocus((audioFocusRequest)!!)
                        } else {
                            audioFocusResult = audioManager!!.requestAudioFocus(
                                audioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN
                            )
                        }
                        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
                    }
                    mediaSession!!.isActive = true // Сразу после получения фокуса
                    registerReceiver(
                        becomingNoisyReceiver,
                        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                    )
                    exoPlayer!!.playWhenReady = true
                }
                mediaSession!!.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PLAYING
                refreshNotificationAndForegroundStatus(currentState)
            }

            override fun onPause() {
                if (exoPlayer!!.playWhenReady) {
                    exoPlayer!!.playWhenReady = false
                    unregisterReceiver(becomingNoisyReceiver)
                }
                mediaSession!!.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PAUSED
                refreshNotificationAndForegroundStatus(currentState)
            }

            override fun onStop() {
                if (exoPlayer!!.playWhenReady) {
                    exoPlayer!!.playWhenReady = false
                    unregisterReceiver(becomingNoisyReceiver)
                }
                if (audioFocusRequested) {
                    audioFocusRequested = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager!!.abandonAudioFocusRequest((audioFocusRequest)!!)
                    } else {
                        audioManager!!.abandonAudioFocus(audioFocusChangeListener)
                    }
                }
                mediaSession!!.isActive = false
                mediaSession!!.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_STOPPED
                refreshNotificationAndForegroundStatus(currentState)
                stopSelf()
            }

            override fun onSkipToNext() {
                val track = tracks.next
                updateMetadataFromTrack(track)
                refreshNotificationAndForegroundStatus(currentState)
                prepareToPlay(track.trackUri)
            }

            override fun onSkipToPrevious() {
                val track = tracks.previous
                updateMetadataFromTrack(track)
                refreshNotificationAndForegroundStatus(currentState)
                prepareToPlay(track.trackUri)
            }

            private fun prepareToPlay(uri: String) {
                if (uri != currentUri) {
                    currentUri = uri
                    val mediaItem = MediaItem.fromUri(uri)
                    exoPlayer?.apply {
                        setMediaItem(mediaItem)
                        prepare()
                    }
                }
            }

            private fun updateMetadataFromTrack(track: Track) {
                val theBitmap: Bitmap = Glide
                    .with(applicationContext)
                    .asBitmap()
                    .load(track.bitmapUri)
                    .submit()
                    .get()

                metadataBuilder.apply {
                    putBitmap(MediaMetadataCompat.METADATA_KEY_ART, theBitmap)
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.artist)
                    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
                    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.duration)
                    mediaSession!!.setMetadata(this.build())
                }
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

    override fun onBind(intent: Intent): IBinder? {
        return PlayerServiceBinder()
    }

    inner class PlayerServiceBinder() : Binder() {
        val mediaSessionToken: MediaSessionCompat.Token
            get() = mediaSession!!.sessionToken
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
        val builder: NotificationCompat.Builder = MediaStyleHelper.from(this, mediaSession)
        builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_previous,
                getString(R.string.previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
        )
        if (playbackState == PlaybackStateCompat.STATE_PLAYING) builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        ) else builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        )
        builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_next,
                getString(R.string.next),
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
                .setMediaSession(mediaSession!!.sessionToken)
        ) // setMediaSession требуется для Android Wear
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.color = ContextCompat.getColor(
            this,
            R.color.colorPrimaryDark
        ) // The whole background (in MediaStyle), not just icon background
        builder.setShowWhen(false)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setOnlyAlertOnce(true)
        builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID)
        return builder.build()
    }
}
