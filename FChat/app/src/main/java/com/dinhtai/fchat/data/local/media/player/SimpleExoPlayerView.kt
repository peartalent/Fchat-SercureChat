package com.dinhtai.fchat.data.local.media.player

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

class  SimpleExoPlayerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : PlayerView(context, attrs, defStyleAttr), Player.EventListener {

    private var simpleExoPlayer: SimpleExoPlayer
    var playerView:PlayerView? = null

    init {
        playerView = PlayerView(context)
        playerView?.controllerHideOnTouch =false
        playerView?.controllerShowTimeoutMs=0
        var x = PlayerControlView(context)
        
        addView(playerView)
        simpleExoPlayer = SimpleExoPlayer.Builder(context).build()

        simpleExoPlayer.addListener(this)
        simpleExoPlayer.playWhenReady = false
        playerView?.player = simpleExoPlayer
    }

    fun prepare(uri: Uri){
        val timeout = 10000
        val dataSourceFactory = DefaultHttpDataSourceFactory(
                Util.getUserAgent(context, "ExoPlayerView"),
                timeout,
                timeout,
                true)
        val mediaSource = when(Util.inferContentType(uri)){
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory)
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
            else -> throw Exception("Fuente desconocida")
        }.createMediaSource(uri)
        simpleExoPlayer.prepare(mediaSource)
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Log.e("ExoPlayer", "Error: ", error)
    }
}
