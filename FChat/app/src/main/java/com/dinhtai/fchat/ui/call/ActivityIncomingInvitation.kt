package com.dinhtai.fchat.ui.call

import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ActivityIncomingInvitationBinding
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.service.RingService
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
import org.json.JSONObject
import java.net.URL
import kotlin.coroutines.CoroutineContext

class ActivityIncomingInvitation : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityIncomingInvitationBinding
    private var meetingType: String? = null
    private var user: User? = null
    private var mWebSocket: WebSocket? = null
    private val TAG = "activityincomming"
    private lateinit var mediaPlayer: MediaPlayer
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private var isConnect = false
    private lateinit var serviceMusic: RingService
    private val connectServiceMusic = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnect = false
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val myBinder = p1 as RingService.MyBinder
            serviceMusic = myBinder.getMusicControl()
            isConnect = true
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        meetingType = intent.getStringExtra(Constant.REMOTE_MSG_MEETING_TYPE)
        Intent(this, RingService::class.java).also { intent ->
            bindService(
                intent,
                connectServiceMusic,
                Context.BIND_AUTO_CREATE
            )
        }
//
//        mediaPlayer = MediaPlayer()
//        mediaPlayer.setMediaAsset("music/music_ring.mp3")
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
//        mediaPlayer.prepare();
//        mediaPlayer.start();

        if (meetingType != null) {
            if (meetingType == Constant.REMOTE_MSG_VALUE_TYPE_VIDEO) {
                binding.imageMeetingType.setImageResource(R.drawable.ic_record)
            } else {
                binding.imageMeetingType.setImageResource(R.drawable.ic_mic)
            }
        }

        WebSocketClient().connectSocket(ConfigAPI.WS_URL_CALL, SocketListener())
        val sharedPref = this?.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
        InfoYourself.token = sharedPref.getString(getString(R.string.token), null)
        var json = JsonObject()
        InfoYourself.token?.let { token ->
            json.addProperty("token", token)
            json.addProperty("type_data", ConfigMessage.USER_LOGIN)
            sendMessage(json.toString())
        }
        user = (intent.getSerializableExtra("user") as User)
        binding.imageAcceptInvitation.setOnClickListener {
            serviceMusic.stopMediaPlayer()
//            mediaPlayer.stop()
//            mediaPlayer.prepare()
            sendInvitationResponse(
                Constant.REMOTE_MSG_INVITATION_ACCEPTED,
                (intent.getSerializableExtra("user") as User).userId
            )
        }
        binding.imageRejectInvitation.setOnClickListener {
            serviceMusic.stopMediaPlayer()
            sendInvitationResponse(
                Constant.REMOTE_MSG_INVITATION_REJECTED,
                (intent.getSerializableExtra("user") as User).userId
            )
            finish()
        }
        Handler().postDelayed(object : Runnable {
            override fun run() {
                serviceMusic.stopMediaPlayer()
                sendInvitationResponse(
                    Constant.REMOTE_MSG_INVITATION_REJECTED,
                    (intent.getSerializableExtra("user") as User).userId
                )
                finish()
            }

        }, 30*1000)
        Log.d(TAG, (intent.getSerializableExtra("user") as User).toString())
    }

    private fun sendInvitationResponse(type: String, userId: String?) {
        try {
            val body = JSONObject()
            val data = JSONObject()

            InfoYourself.token?.let { token ->
                body.put("token", token)
                body.put("type_data", ConfigMessage.USER_CALL_VIDEO_ONE_CLIENT)
                body.put("type_chat", "user")
            }
            data.put(Constant.REMOTE_MSG_TYPE, Constant.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constant.REMOTE_MSG_INVITATION_RESPONSE, type)
            body.put(Constant.REMOTE_MSG_DATA, data)
            var member = Gson().toJson(listOf<User>(User(userId!!, null, null, null, null)))
            body.put("members", member)
            sendRemoteMessage(body.toString(), type)
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendRemoteMessage(data: String, type: String) {
        sendMessage(data)
        if (type == Constant.REMOTE_MSG_INVITATION_ACCEPTED) {
            try {
                val serverURL = URL("https://meet.jit.si")
                val builder = JitsiMeetConferenceOptions.Builder()
                builder.setServerURL(serverURL)
                builder.setWelcomePageEnabled(false)
                builder.setRoom(intent.getStringExtra(Constant.REMOTE_MSG_MEETING_ROOM))
                user?.let {
                    var uInfo = JitsiMeetUserInfo()
                    uInfo.avatar = URL("http://${ConfigAPI.URL}${it?.avatar}")
                    uInfo.displayName = (InfoYourself?.fullname)
                    builder.setUserInfo(uInfo)
                }
                builder.apply {
                    setFeatureFlag("pip.enabled", false) // <- this line you have to add
                    setFeatureFlag("calendar.enabled", false)  // optional
                    setFeatureFlag("call-integration.enabled", false)  // optional
                    setFeatureFlag("pip.enabled", false)
                    setFeatureFlag("calendar.enabled", false)
                    setFeatureFlag("call-integration.enabled", false)
                    setFeatureFlag("close-captions.enabled", false)
                    setFeatureFlag("chat.enabled", false)
                    setFeatureFlag("invite.enabled", false)
                    setFeatureFlag("live-streaming.enabled", false)
                    setFeatureFlag("meeting-name.enabled", false)
                    setFeatureFlag("meeting-password.enabled", false)
                    setFeatureFlag("raise-hand.enabled", false)
                    setFeatureFlag("video-share.enabled", false)
                }
                if (meetingType == Constant.REMOTE_MSG_VALUE_TYPE_VIDEO) {
                    builder.setVideoMuted(true)
                } else if (meetingType == Constant.REMOTE_MSG_VALUE_TYPE_AUDIO) {
                    builder.setAudioOnly(true)
                }
                JitsiMeetActivity.launch(this@ActivityIncomingInvitation, builder.build())
                finish()
            } catch (e: Exception) {
                toast(e.message.toString())
                finish()
            }
        } else {
            toast("Invitation Rejected")
            finish()
        }
    }

    private fun sendMessage(data: String) = runBlocking {
        Log.d(TAG, data)
        mWebSocket?.send(data)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isConnect) {
            unbindService(connectServiceMusic)
            isConnect = false
        }
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseReceiver
        )
    }

    private fun MediaPlayer.setMediaAsset(fileAsset: String) {
        val afd = assets.openFd(fileAsset)
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.d("message socket chatactivity", text)
            InfoYourself.token?.let { token ->
            }
        }
    }

    private val invitationResponseReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getStringExtra(Constant.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null) {
                if (type == Constant.REMOTE_MSG_INVITATION_CANCELLED) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show()
                    finish()
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
}
