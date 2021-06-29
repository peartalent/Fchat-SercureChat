package com.dinhtai.fchat.ui.call

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.utils.config.ConfigAPI
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class CallViewModel(val context: Context) : RxViewModel() {
    private var _webSocket = MutableLiveData<WebSocket>()
    fun mWebSocket() : LiveData<WebSocket>{
        return _webSocket
    }
    init {
        WebSocketClient().connectSocket(ConfigAPI.WS_URL_CALL, SocketListener())
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            _webSocket.value = webSocket
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            val json = JSONObject(text)
            val intent = Intent(Constant.REMOTE_MSG_INVITATION_RESPONSE)
            intent.putExtra(
                Constant.REMOTE_MSG_INVITATION_RESPONSE,
                json.getString(Constant.REMOTE_MSG_INVITATION_RESPONSE)
            )
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
