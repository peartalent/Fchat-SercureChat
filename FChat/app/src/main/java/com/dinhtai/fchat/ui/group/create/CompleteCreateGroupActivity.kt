package com.dinhtai.fchat.ui.group.create

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dinhtai.fchat.ui.main.MainActivity
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.KeyAES
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ActivityCompleteCreateGroupBinding
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.ui.baseui.adapter.MemberGroupAdapter
import com.dinhtai.fchat.utils.config.ConfigAPI
import com.dinhtai.fchat.utils.config.ConfigMessage
import com.dinhtai.fchat.utils.encodeImage
import com.dinhtai.fchat.utils.file.FileExternal
import com.dinhtai.fchat.utils.safe.AESCrypt
import com.dinhtai.fchat.utils.safe.RSACrypt
import com.dinhtai.fchat.utils.toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.*
import kotlin.coroutines.CoroutineContext

class CompleteCreateGroupActivity : AppCompatActivity(), CoroutineScope {
    private var users: MutableList<User> = mutableListOf()
    private val viewModel: CreateGroupViewModel by lazy {
        ViewModelProvider(this)[CreateGroupViewModel::class.java]
    }
    private var mWebSocket: WebSocket? = null
    private lateinit var binding: ActivityCompleteCreateGroupBinding
    private var encodeImage: String? = null
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompleteCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var intent = getIntent()
        WebSocketClient().connectSocket(ConfigAPI.WS_URL_CHAT, SocketListener())

        users.addAll(intent.getSerializableExtra("member") as MutableList<User>)
        binding.createGroupVM = viewModel
        viewModel.setUsers(users)
        binding.recyclerFriend.adapter = MemberGroupAdapter(::onAddItemClick)
        binding.textMember.text = resources.getString(R.string.lable_member) + " " + users.size
        Log.d("member ", users.toString())
        var stringMember = mutableListOf<String>()
        users.forEach {
            stringMember.add(it.userId)
        }
        Log.d("member", stringMember.toString().replace(" ", ""))
        binding.buttonDone.setOnClickListener {
            if (binding.editNameGroup.text.toString().trim().length > 0) {
               // binding.buttonDone.
                InfoYourself.token?.let {
                    viewModel.createGroup(
                        it,
                        binding.editNameGroup.text.toString().trim(),
                        stringMember.toString().replace(" ", ""),
                        encodeImage
                    )
                }
            } else {
                toast("Vui lòng nhập tên group muốn tạo!!")
            }
        }
        binding.imageUpAvatar.setOnClickListener {
            showPopup(it)
        }
        viewModel.status.observe(this, {
            it?.let { response ->
                if (response.status == 1) {
                    Log.d("dddd", "thành công")
                    toast("Thành công")
                    // code là group id
                    mWebSocket?.let {
                        var fileExternal = FileExternal(applicationContext)
                        if (fileExternal.readFileAES("group"+response.code.toString()) == null || fileExternal.readFileAES(
                                "group"+response.code.toString()
                            )!!.key.isNullOrEmpty()
                        ) {
                            var key = AESCrypt().generateKey
                            fileExternal.writeFileAES(
                                Gson().toJson(
                                    KeyAES(
                                        "group"+response.code.toString(),
                                        key,
                                        Date().toString()
                                    )
                                ),"group"+response.code.toString()
                            )
                            users.forEach {  u ->
                                var pubKey = RSACrypt().covertPublicKey(u.publicKey!!);
                                send(sendKey(
                                    RSACrypt().getEncryptedString(pubKey, key)?:"",
                                    "create_key_secret",
                                    response.code,
                                    u.userId
                                ))
                            }

                        }
                    }

                    var intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Log.d("dddd", "Thát bại")
                    toast("Đã có lỗi xảy ra")
                }
            }
        })
    }
    private fun send(data: String) = runBlocking {
        mWebSocket?.send(data)
    }
    private fun onAddItemClick(user: User) {

    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_choose_image_avatar)

        popup.setOnMenuItemClickListener({ item: MenuItem? ->
            when (item!!.itemId) {
                R.id.itemLibrary -> {
                    requestPermionAndPickImage()
                }
            }
            true
        })
        popup.show()
    }

    //Yêu cầu quyền truy câp
    private fun requestPermionAndPickImage() {
        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                startCropImageActivity()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
//                thất bại
                toast("Denied")
            }
        }
        TedPermission.with(this)
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check();
    }

    private fun startCropImageActivity() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(4,4)
            .start(this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                encodeImage = encodeImage(resultUri)
                binding.imageUpAvatar.setImageURI(resultUri)
                encodeImage?.let { Log.d("ddddd", it) }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun sendKey(
        content: String,
        typeMessege: String,
        groupId:Int,
        receiverId: String
    ): String {
        var json = JsonObject()
        json.addProperty("group_id",groupId)
        json.addProperty("type_chat", "user")
        json.addProperty("token", InfoYourself.token)
        json.addProperty("type_data", typeMessege)
        json.addProperty("content", content)
        json.addProperty("receiver_id", receiverId)
        return json.toString()
    }


    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
            this@CompleteCreateGroupActivity?.runOnUiThread(Runnable {
                var json = JsonObject()
                InfoYourself.token?.let { token ->
                    json.addProperty("token", token)
                    json.addProperty("type_data", ConfigMessage.USER_LOGIN)
                    webSocket.send(json.toString())
                }
            })
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)

        }
    }

}
