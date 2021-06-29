package com.dinhtai.fchat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dinhtai.fchat.ui.call.ActivityIncomingInvitation
import com.dinhtai.fchat.ui.main.MainActivity
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.KeyAES
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.ui.call.Constant
import com.dinhtai.fchat.utils.file.FileExternal
import com.dinhtai.fchat.utils.safe.RSACrypt
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import java.util.*

class FirebaseMessageService : FirebaseMessagingService() {
    private val TAG = "firebaseService"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            Log.d("service ne",remoteMessage.data[Constant.REMOTE_MSG_INVITATION_RESPONSE].toString() )
            when (remoteMessage.data["type"]) {
                TYPE_NOTIFICATION_CREATE_KEY_SECRET -> {
                    val sharedPref = this?.getSharedPreferences(
                        getString(R.string.app_name), Context.MODE_PRIVATE
                    )
                    var userId = remoteMessage.data["user_id"].toString()
                    var fileExternal = FileExternal(applicationContext)
                    Log.d("rsa ",sharedPref.getString("phone", null).toString())

                    var rsa = fileExternal.readFileRSA(sharedPref.getString("phone", null).toString())
                    Log.d("rsa ",rsa.toString())
                    var nameFile =userId
                    if (remoteMessage.data["title"].toString().contains("group")){
                        nameFile = remoteMessage.data["title"].toString()
                    }

                    var keyRSA = RSACrypt().covertKeyPair(rsa!!.publicKey,rsa!!.privateKey)
                    var x = RSACrypt().getEncryptedString(keyRSA.public,"xin chao")
                    var y = RSACrypt().getDecryptedString(keyRSA.private,x)
                    Log.d("key test",x + " ccc "+ y)
                    var keyAES = RSACrypt().getDecryptedString(keyRSA.private,remoteMessage.data["key"].toString())
                    
                    Log.d("key aes",keyAES?:"nulll nef" +remoteMessage.data["key"].toString() )
                    fileExternal.writeFileAES(
                        Gson().toJson(
                            KeyAES(
                                nameFile,
                                keyAES?:"",
                                Date().toString()
                            )
                        ), nameFile
                    )
                    Log.d("xxxxxxxxxxxxxxxxxxxxx",remoteMessage.toString())
                }
                TYPE_NOTIFICATION_MESSAGE -> {
                    sendNotification(
                        remoteMessage.data["title"].toString(),
                        remoteMessage.data["body"].toString(),
                        21221221
                    )
                }
                TYPE_NOTIFICATION_INVITATION -> {
                    var intent = Intent(applicationContext, ActivityIncomingInvitation::class.java)
                    intent.putExtra("title", remoteMessage.data["title"].toString())
                    intent.putExtra(
                        Constant.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.data[Constant.REMOTE_MSG_MEETING_ROOM]
                    )
                    intent.putExtra(
                        "user", User(
                            userId = remoteMessage.data["user_id"].toString(),
                            fullname = remoteMessage.data["fullname"].toString(),
                            avatar = remoteMessage.data["avatar"].toString(),
                            publicKey = null, lastOnline = null, status = null
                        )
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                TYPE_NOTIFICATION_INVITATION_RESPONSE -> {
                    val intent = Intent(Constant.REMOTE_MSG_INVITATION_RESPONSE)
                    intent.putExtra(
                        Constant.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.data[Constant.REMOTE_MSG_INVITATION_RESPONSE]
                    )
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
                else -> Log.d(TAG, "thoát")
            }
        }
    }

    private fun sendNotification(title: String, body: String, chanelId: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val channelId = getString(R.string.project_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.begin)
            .setColor(resources.getColor(R.color.cerulean))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setWhen(System.currentTimeMillis())
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(chanelId, notificationBuilder.build())
    }

    companion object {
        private const val TYPE_NOTIFICATION_MESSAGE = "message"
        private const val TYPE_NOTIFICATION_INVITATION = "invitation"
        private const val TYPE_NOTIFICATION_INVITATION_RESPONSE = "invitationResponse"
        private const val TYPE_NOTIFICATION_CREATE_KEY_SECRET = "create_key_secret"
    }
}
