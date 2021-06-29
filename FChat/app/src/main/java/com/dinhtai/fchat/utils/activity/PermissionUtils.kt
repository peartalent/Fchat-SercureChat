package com.dinhtai.fchat.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun Activity.checkAudioPermission(requestCode: Int): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!isGranted(Manifest.permission.RECORD_AUDIO)) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                requestCode
            )
            return false
        }
    }
    return true
}

fun Activity.checkContactPermission(requestCode: Int): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!isGranted(Manifest.permission.READ_CONTACTS)) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                requestCode
            )
            return false
        }
    }
    return true
}

fun Activity.checkGPSPermission(requestCode: Int): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || !isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
                requestCode
            )
            return false
        }
    }
    return true
}

fun Context.isGranted(permissionCode: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        checkSelfPermission(permissionCode) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
