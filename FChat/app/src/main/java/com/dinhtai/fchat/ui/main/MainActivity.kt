package com.dinhtai.fchat.ui.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.dinhtai.fchat.Begin
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.databinding.ActivityMainBinding
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.utils.*
import com.dinhtai.fchat.utils.config.ConfigAPI
import com.dinhtai.fchat.utils.config.ConfigMessage
import com.dinhtai.fchat.utils.file.FileExternal
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private val navController by lazy { findNavController(R.id.navHostFragment) }
    private lateinit var binding: ActivityMainBinding
    private var TOKEN: String? = ""
    private var token_device: String? = null
    private var mWebSocket: WebSocket? = null
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469
    private val REAS_CONTACT_PERMISSION = 121
    private val OVERLAY_REQUEST_CODE = 6000
    val viewModel: ActivityMainViewModel by lazy {
        ViewModelProvider(this)[ActivityMainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("activity", "đã qua activity")
        setupViews()
        auth = FirebaseAuth.getInstance()
        var currentUser = auth.currentUser
        val sharedPref = this?.getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
        TOKEN = sharedPref.getString(getString(R.string.token), "fail")
        Log.d("token file", TOKEN ?: "fail")
        InfoYourself.token = TOKEN
        InfoYourself.userID = sharedPref.getString(getString(R.string.user_id), null)
        InfoYourself.phone = sharedPref.getString("phone", null)
        viewModel.login(InfoYourself.token!!)
        viewModel.statusLogin.observe(this,{
            it?.let {
                if (it.status == 0) {
                    auth.signOut()
                    var intent = Intent(this, Begin::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        })
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("token", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            token_device = task.result
            if (currentUser == null || TOKEN.equals("fail") ||
                InfoYourself.token == null || InfoYourself.phone == null ||
                FileExternal(applicationContext).readFileRSA(InfoYourself.phone!!) == null
            ) {

                auth.signOut()
                var intent = Intent(this, Begin::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                //checkForBatteryOptimizations()
                if (checkContactPermission(REAS_CONTACT_PERMISSION)) {
                    var contacts = Gson().toJson(getContacts())
                    viewModel.setContact(InfoYourself.token!!, contacts)
                    var fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(this)
                    if (checkGPSPermission(111)) {
                        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                            Log.d(
                                "tọa độ",
                                location?.latitude.toString() + "," + location?.longitude.toString()
                            )
                            location?.let {
                                var myLocation = com.dinhtai.fchat.data.local.Location(
                                    location.latitude,
                                    location.longitude
                                )
                                viewModel.setLocation(InfoYourself.token!!, myLocation)
                            }
                        }
                    }
                }
                InfoYourself.keyRSA =
                    FileExternal(applicationContext).readFileRSA(InfoYourself.phone!!)
                checkPermissionXiaomi()
                WebSocketClient().connectSocket(ConfigAPI.WS_URL_LOGIN, SocketListener())
            }
            Log.d("token xxx", token_device!!)
        })
    }

    private fun setupViews() {
        binding.bottomNavigation.setupWithNavController(navController)
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
            this@MainActivity.runOnUiThread(Runnable {
                Log.d("activity", "connect socket")
                var json = JsonObject()
                json.addProperty("token", TOKEN)
                json.addProperty("token_client", token_device)
                json.addProperty("type_data", ConfigMessage.USER_LOGIN)
                webSocket.send(json.toString())
                Log.d("json", json.toString())
            })

        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.d("message socket activity", text)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun checkForBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Chú ý")
                builder.setMessage("Tối ưu hóa pin được bật. Nó có thể làm gián đoạn các dịch vụ nền đang chạy.")
                builder.setPositiveButton("Tắt tối ưu") { dialogInterface: DialogInterface?, i: Int ->
                    val intent =
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivityForResult(intent, 1)
                }
                builder.setNegativeButton(
                    "Thoát"
                ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                builder.create().show()
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun checkPermissionXiaomi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                if ("xiaomi" == Build.MANUFACTURER.toLowerCase(Locale.ROOT)) {
                    val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                    intent.setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    intent.putExtra("extra_pkgname", packageName)
                    AlertDialog.Builder(this)
                        .setTitle("Bạn đang dùng điện thoại Xiaomi")
                        .setMessage("Bạn sẽ không nhận được thông báo khi ứng dụng ở chế độ nền nếu bạn tắt các quyền này")
                        .setPositiveButton(
                            "Settings"
                        ) { dialog, which -> startActivity(intent) }
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setCancelable(false)
                        .show()
                } else {
                    val overlaySettings = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                            "package:$packageName"
                        )
                    )
                    startActivityForResult(
                        overlaySettings,
                        ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            checkForBatteryOptimizations()
        }
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                checkPermission()
            } else {
                finish()
            }
        }
    }
}
