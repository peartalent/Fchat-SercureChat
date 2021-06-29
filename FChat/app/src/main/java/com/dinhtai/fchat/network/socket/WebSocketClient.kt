package com.dinhtai.fchat.network.socket

import okhttp3.*
import okhttp3.WebSocket

class WebSocketClient {
    private var _webSocket: WebSocket? = null
    val webSocket : WebSocket?
        get() = _webSocket

    fun connectSocket(url:String,listener: WebSocketListener){
        var client = OkHttpClient()
        client.retryOnConnectionFailure()
        var request = Request.Builder().url(url).build()
        client.newWebSocket(request, listener)
    }
}
