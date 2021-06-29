package com.dinhtai.fchat.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.ByteArrayOutputStream

fun Context.showToast(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_LONG).apply { show() }
}

fun Context.getDrawableCompat(@DrawableRes resId: Int) =
    ContextCompat.getDrawable(this, resId)

fun Context.mediaSource(uri: Uri, timeout: Int = 10000): MediaSource {
    val dataSourceFactory = DefaultHttpDataSourceFactory(
        Util.getUserAgent(this, "PlayerView"),
        timeout,
        timeout,
        true)
    return when(Util.inferContentType(uri)){
        C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory)
        C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
        C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
        C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
        else -> throw Exception("Fuente desconocida")
    }.createMediaSource(uri)
}
fun Context.encodeImage(imageUri: Uri): String {
    Log.d("uri image", imageUri.toString())
    val input = this.getContentResolver().openInputStream(imageUri)
    val image = BitmapFactory.decodeStream(input, null, null)
    // Encode image to base64 string
    val baos = ByteArrayOutputStream()
    image?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    var imageBytes = baos.toByteArray()
    val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
    return imageString
}
