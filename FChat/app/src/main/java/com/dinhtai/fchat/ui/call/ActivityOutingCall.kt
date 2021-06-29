package com.dinhtai.fchat.ui.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ActivityOutingCallBinding
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.utils.config.ConfigAPI
import com.dinhtai.fchat.utils.config.ConfigMessage
import com.dinhtai.fchat.utils.toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import org.jitsi.meet.sdk.JitsiMeetView
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.coroutines.CoroutineContext

class ActivityOutingCall : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityOutingCallBinding
    private var user: User? = null
    private var groupId: Int = -1
    private var callType: String? = null
    private var totalReceivers: Int = 0
    private var rejectionCount = 0
    private var mWebSocket: WebSocket? = null
    private var meetRoom: String? = null
    private val TAG = "activityoutingcall"
    private var members: List<User>? = null

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        user = intent.getSerializableExtra("user")?.let { it as User }
        groupId = intent.getIntExtra("groupId", -1)
        members = intent.getSerializableExtra("members") as List<User>
        callType = intent.getStringExtra("type")
        user?.let { binding.user = it }
        if (callType != null) {
            if (callType == Constant.REMOTE_MSG_VALUE_TYPE_VIDEO) {
                binding.imageMeetingType.setImageResource(R.drawable.ic_record)
            } else {
                binding.imageMeetingType.setImageResource(R.drawable.ic_mic)
            }
        }
        WebSocketClient().connectSocket(ConfigAPI.WS_URL_CALL, SocketListener())

        binding.imageStopInvitation.setOnClickListener {
            cancelInvitation(members)
        }
        Handler().postDelayed(object : Runnable {
            override fun run() {
                cancelInvitation(members)
            }

        }, 40*1000)
    }

    private fun cancelInvitation(receivers: List<User>?) {
        try {
            val body = JSONObject()
            val data = JSONObject()
            val receiver = Gson().toJson(receivers)
            InfoYourself.token?.let { token ->
                body.put("token", token)
                if (groupId == -1) {
                    body.put("type_chat", "user")
                } else {
                    body.put("type_chat", "group")
                }
                body.put("type_data", ConfigMessage.USER_CALL_VIDEO_ONE_CLIENT)
            }
            data.put(Constant.REMOTE_MSG_TYPE, Constant.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(
                Constant.REMOTE_MSG_INVITATION_RESPONSE,
                Constant.REMOTE_MSG_INVITATION_CANCELLED
            )
            body.put(Constant.REMOTE_MSG_DATA, data)
            body.put("members", receiver)
            sendRemoteMessage(body.toString())
            finish()
        } catch (e: java.lang.Exception) {
            toast(e.message.toString())
            finish()
        }
    }

    private fun initiateMeeting(callType: String, receivers: List<User>?) {
        try {
            // check recceivers sau
            val body = JSONObject()
            val data = JSONObject()
            val receiver = Gson().toJson(receivers)
            InfoYourself.token?.let { token ->
                body.put("token", token)
                if (groupId == -1) {
                    body.put("type_chat", "user")
                } else {
                    body.put("type_chat", "group")
                }
                body.put("type_data", ConfigMessage.USER_CALL_VIDEO_ONE_CLIENT)
            }
      //     body.put("user_id", user!!.userId)
            body.put("members", receiver)
            data.put(Constant.REMOTE_MSG_TYPE, Constant.REMOTE_MSG_INVITATION)
            data.put(Constant.REMOTE_MSG_MEETING_TYPE, callType)
            meetRoom = "user_id" + "_" +
                    UUID.randomUUID().toString()
            data.put(Constant.REMOTE_MSG_MEETING_ROOM, meetRoom)
            body.put("content", data)
            sendRemoteMessage(body.toString())
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendRemoteMessage(data: String) = runBlocking {
        Log.d(TAG, data)
        mWebSocket?.send(data)
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
            var json = JsonObject()
            InfoYourself.token?.let { token ->
                json.addProperty("token", token)
                json.addProperty("type_data", ConfigMessage.USER_LOGIN)
                sendRemoteMessage(json.toString())
            }
            callType?.let {
                totalReceivers = 1
                initiateMeeting(it, members)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            val json = JSONObject(text)
            val intent = Intent(Constant.REMOTE_MSG_INVITATION_RESPONSE)
            intent.putExtra(
                Constant.REMOTE_MSG_INVITATION_RESPONSE,
                json.getString(Constant.REMOTE_MSG_INVITATION_RESPONSE)
            )
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        }
    }

    private val invitationResponseReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getStringExtra(Constant.REMOTE_MSG_INVITATION_RESPONSE)
            type?.let { Log.d("meet ....", it) }
            type?.let {
                if (type == Constant.REMOTE_MSG_INVITATION_ACCEPTED) {
                    try {
                        val serverURL = URL("https://meet.jit.si")

                        val builder = JitsiMeetConferenceOptions.Builder()
                            .setServerURL(serverURL)
                            .setWelcomePageEnabled(false)
                            .setRoom(meetRoom)
                        user?.let {
                            var uInfo= JitsiMeetUserInfo()
                            uInfo.displayName = InfoYourself?.fullname
                            builder.setUserInfo(uInfo)
                        }
                        builder.apply {
                            setFeatureFlag("pip.enabled",false) // <- this line you have to add
                            setFeatureFlag("calendar.enabled",false)  // optional
                            setFeatureFlag("call-integration.enabled",false)  // optional
                            setFeatureFlag("pip.enabled",false)
                            setFeatureFlag("calendar.enabled",false)
                            setFeatureFlag("call-integration.enabled",false)
                            setFeatureFlag("close-captions.enabled",false)
                            setFeatureFlag("chat.enabled",false)
                            setFeatureFlag("invite.enabled",false)
                            setFeatureFlag("live-streaming.enabled",false)
                            setFeatureFlag("meeting-name.enabled",false)
                            setFeatureFlag("meeting-password.enabled",false)
                            setFeatureFlag("raise-hand.enabled",false)
                            setFeatureFlag("video-share.enabled",false)
                        }
                        if (callType == Constant.REMOTE_MSG_VALUE_TYPE_VIDEO) {
                            builder.setVideoMuted(true)
                        } else if (callType == Constant.REMOTE_MSG_VALUE_TYPE_AUDIO) {
                            builder.setAudioOnly(true)
                        }
                        JitsiMeetActivity.launch(this@ActivityOutingCall, builder.build())
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@ActivityOutingCall,
                            e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else if (type == Constant.REMOTE_MSG_INVITATION_REJECTED) {
                    rejectionCount += 1
                    if (rejectionCount == totalReceivers) {
                        Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver,
            IntentFilter(Constant.REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseReceiver
        )
    }
}
