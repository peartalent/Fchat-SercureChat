package com.dinhtai.fchat.service

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class RingService : Service() {
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMediaPlayer()
        return START_NOT_STICKY
    }
    fun startMediaPlayer() {
        mediaPlayer!!.setMediaAsset("music/music_ring.mp3")
      //  mediaPlayer.set
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
        mediaPlayer!!.prepare();
        mediaPlayer!!.start();
    }
    fun stopMediaPlayer() {
        mediaPlayer.apply {
            stop()
            release()
        }
    }
    override fun onBind(p0: Intent?): IBinder? = MyBinder()

    override fun unbindService(conn: ServiceConnection) {
        super.unbindService(conn)
    }


    inner class MyBinder : Binder() {
        fun getMusicControl(): RingService = this@RingService
    }
    private fun MediaPlayer.setMediaAsset(fileAsset: String) {
        val afd = assets.openFd(fileAsset)
        mediaPlayer?.let {
            it.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        }
    }
}
