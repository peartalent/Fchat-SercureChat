package com.dinhtai.fchat.utils.config

import android.net.Uri

object ConfigAPI {
    const val PORT = 3000
//     const val HOST_ADDRESS = "sg.mdcsoftware.com.vn"
 //  const val HOST_ADDRESS = "192.168.100.131"
 //   const val HOST_ADDRESS = "192.168.43.141"
 //   const val  HOST_ADDRESS = "51.79.157.249"
    const val HOST_ADDRESS = "192.168.0.101"
    const val PATH_LOGIN = "login"
    const val PATH_CHAT = "chat"
    const val PATH_CALL = "call"
    val URL = "http://$HOST_ADDRESS:$PORT"
    val WS_URL_LOGIN = "http://$HOST_ADDRESS:$PORT/$PATH_LOGIN"
    val WS_URL_CHAT = "http://$HOST_ADDRESS:$PORT/$PATH_CHAT"
    val WS_URL_CALL = "http://$HOST_ADDRESS:$PORT/$PATH_CALL"
}

fun getUri(url: String): Uri = Uri.parse(ConfigAPI.URL + url)
