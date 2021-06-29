//package com.dinhtai.fchat.webrtc
//
//import android.util.Log
//import com.dinhtai.fchat.data.local.InfoYourself
//import com.dinhtai.fchat.data.local.User
//import com.dinhtai.fchat.network.socket.WebSocketClient
//import com.dinhtai.fchat.ui.call.CallVideoActivity
//import com.dinhtai.fchat.utils.config.ConfigMessage
//import com.google.gson.Gson
//import com.google.gson.JsonObject
//import io.ktor.http.cio.websocket.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.runBlocking
//import okhttp3.Response
//import okhttp3.WebSocket
//import okhttp3.WebSocketListener
//import okio.ByteString
//import org.webrtc.IceCandidate
//import org.webrtc.SessionDescription
//import kotlin.coroutines.CoroutineContext
//
//class SignallingClientWebRTC(
//    private val user: User  ,
//    private val callVideoActivity: CallVideoActivity
//) : WebSocketListener(), CoroutineScope {
//    var webSocket: WebSocket? = null
//    private val gson = Gson()
//    private val job = Job()
//    override val coroutineContext = Dispatchers.IO + job
//
//    override fun onOpen(webSocket: WebSocket, response: Response) {
//        super.onOpen(webSocket, response)
//        this.webSocket = webSocket
//        var json = JsonObject()
//        callVideoActivity.runOnUiThread {
//            InfoYourself.token?.let { token ->
//                json.addProperty("token", token)
//                json.addProperty("type_data", ConfigMessage.USER_LOGIN)
//                webSocket.send(json.toString())
//            }
//        }
//
//    }
//
//    override fun onMessage(webSocket: WebSocket, text: String) {
//        super.onMessage(webSocket, text)
//        Log.v("Received :", text)
//        val jsonObject = gson.fromJson(text, JsonObject::class.java)
//        if (jsonObject.has("serverUrl")) {
//            listener.onIceCandidateReceived(gson.fromJson(jsonObject, IceCandidate::class.java))
//        } else if (jsonObject.has("type") && jsonObject.get("type").asString == "OFFER") {
//            listener.onOfferReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
//        } else if (jsonObject.has("type") && jsonObject.get("type").asString == "ANSWER") {
//            listener.onAnswerReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
//        }
//    }
//
//    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//        super.onMessage(webSocket, bytes)
//    }
//
//    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
//        super.onClosing(webSocket, code, reason)
//    }
//
//    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//        super.onClosed(webSocket, code, reason)
//    }
//
//    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//        super.onFailure(webSocket, t, response)
//    }
//
//    fun sendData(dataObject: Any?)=runBlocking {
//        Log.v("Sended :", messageCallVideoJson(user, gson.toJson(dataObject)))
//        webSocket?.send(messageCallVideoJson(user, gson.toJson(dataObject)))
//    }
//
//    private fun messageCallVideoJson(user: User, content: String): String {
//        var json = JsonObject()
//        var gson = Gson()
//        json.addProperty("content", content)
//        json.addProperty("token", InfoYourself.token)
//        json.addProperty("type_data", ConfigMessage.USER_CALL_VIDEO_ONE_CLIENT)
//        json.addProperty("user_id", user.userId)
//        return json.toString()
//    }
//
//}
