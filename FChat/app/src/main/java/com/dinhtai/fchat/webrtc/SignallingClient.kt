//package com.dinhtai.fchat.webrtc
//
//import android.util.Log
//import com.dinhtai.fchat.data.local.InfoYourself
//import com.dinhtai.fchat.data.local.User
//import com.dinhtai.fchat.utils.config.ConfigAPI
//import com.dinhtai.fchat.utils.config.ConfigMessage
//import com.google.gson.Gson
//import com.google.gson.JsonObject
//import io.ktor.client.HttpClient
//import io.ktor.client.engine.cio.CIO
//import io.ktor.client.features.json.GsonSerializer
//import io.ktor.client.features.json.JsonFeature
//import io.ktor.client.features.websocket.WebSockets
//import io.ktor.client.features.websocket.ws
//import io.ktor.http.cio.websocket.Frame
//import io.ktor.http.cio.websocket.readText
//import io.ktor.util.KtorExperimentalAPI
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.channels.ConflatedBroadcastChannel
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.withContext
//import org.webrtc.IceCandidate
//import org.webrtc.SessionDescription
//
//@ExperimentalCoroutinesApi
//@KtorExperimentalAPI
//class SignallingClient(
//    private val userId: String
//) : CoroutineScope {
//    private val job = Job()
//
//    private val gson = Gson()
//
//    override val coroutineContext = Dispatchers.IO + job
//
//    private val client = HttpClient(CIO) {
//        install(WebSockets)
//        install(JsonFeature) {
//            serializer = GsonSerializer()
//        }
//    }
//
//    private val sendChannel = ConflatedBroadcastChannel<String>()
//
//    init {
//        connect()
//    }
//
//    private fun connect() = launch {
//        client.ws(host = ConfigAPI.HOST_ADDRESS, port = ConfigAPI.PORT, path = "/${ConfigAPI.PATH_CALL}") {
//            listener.onConnectionEstablished()
//            val sendData = sendChannel.openSubscription()
//            var json = JsonObject()
//            InfoYourself.token?.let { token ->
//                json.addProperty("token", token)
//                json.addProperty("type_data", ConfigMessage.USER_LOGIN)
//                outgoing.send(Frame.Text(json.toString()))
//            }
//            try {
//                while (true) {
//                    //gui
//                    sendData.poll()?.let {
//                        Log.v(this@SignallingClient.javaClass.simpleName, "Sending: ${messageCallVideoJson(userId,it)}")
//                        outgoing.send(Frame.Text(messageCallVideoJson(userId,it)))
//                    }
//                    incoming.poll()?.let { frame ->
//                        if (frame is Frame.Text) {
//                            val data = frame.readText()
//                            Log.v(this@SignallingClient.javaClass.simpleName, "Received : $data")
//                            val jsonObject = gson.fromJson(data, JsonObject::class.java)
//                            Log.v("xxxxxxxxxxx",jsonObject.toString())
//                            withContext(Dispatchers.Main) {
//                                if (jsonObject.has("serverUrl")) {
//                                    listener.onIceCandidateReceived(gson.fromJson(jsonObject, IceCandidate::class.java))
//                                } else if (jsonObject.has("type") && jsonObject.get("type").asString == "OFFER") {
//                                    listener.onOfferReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
//                                } else if (jsonObject.has("type") && jsonObject.get("type").asString == "ANSWER") {
//                                    listener.onAnswerReceived(gson.fromJson(jsonObject, SessionDescription::class.java))
//                                }
//                            }
//                        }
//                    }
//                }
//            } catch (exception: Throwable) {
//                Log.e("asd","asd",exception)
//
//            }
//        }
//    }
//
//    fun send(dataObject: Any?) = runBlocking {
//        sendChannel.send((gson.toJson(dataObject)))
//    }
//
//    fun destroy() {
//        client.close()
//        job.complete()
//    }
//    private fun messageCallVideoJson(userId: String,content:String): String {
//        var json = JsonObject()
//        var gson = Gson()
//        json.addProperty("content",content)
//        json.addProperty("token", InfoYourself.token)
//        json.addProperty("type_data", ConfigMessage.USER_CALL_VIDEO_ONE_CLIENT)
//        json.addProperty("user_id", userId)
//        return json.toString()
//    }
//}
