//package com.dinhtai.fchat.data.local.media.player
//
//import android.net.Uri
//import com.google.android.exoplayer2.C
//import com.google.android.exoplayer2.SimpleExoPlayer
//import com.google.android.exoplayer2.source.ProgressiveMediaSource
//import com.google.android.exoplayer2.source.dash.DashMediaSource
//import com.google.android.exoplayer2.source.hls.HlsMediaSource
//import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
//import com.google.android.exoplayer2.ui.PlayerControlView
//import com.google.android.exoplayer2.ui.PlayerView
//import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
//import com.google.android.exoplayer2.util.Util
//
//class ExoPlayer {
//    private var simpleExoPlayer: SimpleExoPlayer
//    init {
//        simpleExoPlayer = SimpleExoPlayer.Builder(context).build()
//
//        simpleExoPlayer.addListener(this)
//        simpleExoPlayer.playWhenReady = false
//        playerView?.player = simpleExoPlayer
//    }
//
//    fun prepare(uri: Uri){
//        val timeout = 10000
//        val dataSourceFactory = DefaultHttpDataSourceFactory(
//            Util.getUserAgent(context, "ExoPlayerView"),
//            timeout,
//            timeout,
//            true)
//        val mediaSource = when(Util.inferContentType(uri)){
//            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory)
//            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
//            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
//            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
//            else -> throw Exception("Fuente desconocida")
//        }.createMediaSource(uri)
//        simpleExoPlayer.prepare(mediaSource)
//    }
//}
