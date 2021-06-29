package com.dinhtai.fchat.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.dinhtai.fchat.data.local.QrCode
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat

import com.journeyapps.barcodescanner.BarcodeEncoder

fun Context.generateQrCode(content: String): Bitmap? {
    try {
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun Context.isFchatQrCode(content: String): Boolean {
    try {
        var gson = Gson()
        var qrCode = gson.fromJson(content, QrCode::class.java)
        if (qrCode.header == QrCode.HEADER) return true
        else return false
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}
