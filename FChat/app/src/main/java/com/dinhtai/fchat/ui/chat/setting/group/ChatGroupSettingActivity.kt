package com.dinhtai.fchat.ui.chat.setting.group

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.lifecycle.ViewModelProvider
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.databinding.*
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.ui.baseui.adapter.AddFriendToNewGroupAdapter
import com.dinhtai.fchat.ui.baseui.adapter.FriendAdapter
import com.dinhtai.fchat.ui.baseui.adapter.MemberGroupAdapter
import com.dinhtai.fchat.ui.baseui.dialog.*
import com.dinhtai.fchat.ui.chat.ChatActivity
import com.dinhtai.fchat.ui.chat.ChatViewModel
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.ui.group.GroupViewModel
import com.dinhtai.fchat.ui.image.ImageActivity
import com.dinhtai.fchat.ui.main.MainActivity
import com.dinhtai.fchat.ui.profile.qrcode.QrCodeViewModel
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

class ChatGroupSettingActivity : AppCompatActivity(), CoroutineScope {
    private val viewModel: GroupSettingViewModel by lazy {
        ViewModelProvider(this)[GroupSettingViewModel::class.java]
    }
    private val viewModelChat: ChatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }
    private val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private val viewModelQr: QrCodeViewModel by lazy {
        ViewModelProvider(this)[QrCodeViewModel::class.java]
    }
    private val viewModelGroup: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }
    private var mWebSocket: WebSocket? = null
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private var groupId = -1
    private lateinit var binding: ActivitySettingGroupChatBinding
    private var encodeImage: String? = null
    private val fileExternal: FileExternal by lazy { FileExternal(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WebSocketClient().connectSocket(ConfigAPI.WS_URL_CHAT, SocketListener())
        binding.lifecycleOwner = this
        groupId = intent.getIntExtra("groupId", -1)
        binding.settingVM = viewModel
        InfoYourself.token?.let { token ->
            viewModel.getInfoGroupById(token, groupId)
        }
        viewModelChat.getMessageGroupById(InfoYourself.token!!, groupId)
        viewModel.status.observe(this, {
            it?.let {
                if (it.status != 0) {
                    InfoYourself.token?.let { token ->
                        viewModel.getInfoGroupById(token, groupId)
                    }
                    toast("Thành công")
                } else {
                    toast("Thất bại")
                }
            }
        })
        binding.textEditInfo.setOnClickListener {
            viewModel.group.observe(this, {
                it?.let { showDialogEditGroup(it) }
            })
        }
        binding.textMember.setOnClickListener {
            viewModel.group.observe(this, {
                it?.let { it.members?.let { it1 -> showDialogMember(it1) } }
            })
        }
        binding.textAddMember.setOnClickListener {
            viewModel.group.observe(this, {
                it?.let { showDialogAddMember(it) }
            })
        }
        binding.materialToolbar.setNavigationOnClickListener {
            finish()
        }
        binding.textShowImage.setOnClickListener {

            viewModelChat.messageImages.observe(this, {
                it?.let { messages ->
                    DialogBottomSheetShowImage(
                        messages,
                        fileExternal.readFileAES("group" + groupId)!!,
                        this,
                        ::listenerBottomSheet
                    ).show(supportFragmentManager, null)
                }

            })
        }
        binding.textLeaveGroup.setOnClickListener {
            viewModel.deleteMemberToGroup(InfoYourself.token!!, InfoYourself.userID!!, groupId)
            var i = Intent(this, ChatActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
        binding.textDeleteGroup?.setOnClickListener {
            viewModelGroup.deleteGroup(InfoYourself.token!!, groupId)
            var i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

    private fun listenerBottomSheet(item: Message) {
        viewModel.messageImages.value?.forEachIndexed { index, message ->
            if (item.msgId == message.msgId) {
                var i = index
                var intent = Intent(this, ImageActivity::class.java)
                Log.d("item index", i.toString())
                intent.putExtra("index", i)
                if (groupId != -1) {
                    intent.putExtra("type", "group")
                    intent.putExtra("id", groupId)
                }
                startActivity(intent)
            }
        }
    }

    private fun showDialogMember(members: List<User>) {
        val dialog = Dialog(this, R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR)
        dialog.setTitle(resources.getString(com.dinhtai.fchat.R.string.lable_member))
        dialog.setCancelable(true)
        var dialogBinding = DialogMemberBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        var adapter = MemberGroupAdapter({ user ->
            if (user.userId != (InfoYourself.userID)) {
                InfoYourself.token?.let { token ->
                    viewModelFriend.checkFriendOrFollower(token, user.userId, {
                        if (it == 1) {
                            DetailFriendDialog(
                                user,
                                this,
                                this@ChatGroupSettingActivity,
                                {
                                    viewModelFriend.unFriend(InfoYourself.token!!, user.userId)
                                }).show()
                        } else if (it == 0) {
                            DetailUserDialog(user, this, this@ChatGroupSettingActivity, {
                                InfoYourself.token?.let { token ->
                                    viewModelQr.setFollow(
                                        token,
                                        user.userId,
                                        null,
                                        { status ->
                                            toast("Đã gửi thành công")
                                        })
                                }
                            }, {
                            }).show()
                        } else if (it == 2) {
                            DetailFollowerDialog(user, this, this@ChatGroupSettingActivity, {
                                InfoYourself.token?.let { token ->
                                    viewModelFriend.acceptFollow(
                                        token,
                                        user.userId, { }
                                    )
                                }
                            }, {
                                InfoYourself.token?.let { token ->
                                    viewModelFriend.refuseFollow(
                                        token,
                                        user.userId, {}
                                    )
                                }
                            }).show()
                        } else if (it == 3) {
                            DetailFollowedDialog(
                                user,
                                this,
                                this@ChatGroupSettingActivity,
                                {
                                    viewModelFriend.cancelFollow(
                                        InfoYourself.token!!,
                                        user.userId
                                    )
                                }).show()
                        }
                    })
                }
            }
        }, { v, u ->
            viewModel.me?.observe(this, {
                it?.let {
                    if (it.role == AdminDefault.admin && u.role == AdminDefault.default) {
                        showMenuPopup(v, u.userId)
                    }
                }
            })

        })

        adapter.setData(members)
        dialogBinding.recyclerMember.adapter = adapter
        dialogBinding.textSizeMember.text = members.size.toString()
        dialogBinding.materialToolbar.setNavigationOnClickListener {
            recreate()
        }
        dialog.show()
    }

    @SuppressLint("RestrictedApi")
    private fun showMenuPopup(view: View, userId: String) {
        val menuBuilder = MenuBuilder(this)
        val inflater = MenuInflater(this)
        inflater.inflate(com.dinhtai.fchat.R.menu.menu_delete_member, menuBuilder)
        val optionsMenu = MenuPopupHelper(this, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                when (item!!.itemId) {
                    com.dinhtai.fchat.R.id.itemDelete -> {
                        viewModel.deleteMemberToGroup(InfoYourself.token!!, userId, groupId)
                    }
                }
                return false
            }

            override fun onMenuModeChange(menu: MenuBuilder) {}
        })
        optionsMenu.show()
    }

    private fun showDialogEditGroup(group: Group) {
        val dialog = Dialog(this, R.style.ThemeOverlay_Material_Dialog_Alert)
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR)
        dialog.setTitle(resources.getString(com.dinhtai.fchat.R.string.lable_member))
        dialog.setCancelable(true)
        var dialogBinding = DialogEditGroupBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.group = group
        dialogBinding.textAccept.setOnClickListener {
            var name = group.name
            var id = group.id
            var avatar: String? = null
            if (!dialogBinding.editNameGroup.text.isNullOrEmpty()) {
                name = dialogBinding.editNameGroup.text.toString()
            }
            encodeImage?.let { avatar = encodeImage }
            InfoYourself.token?.let {
                viewModel.updateGroupById(it, name, id, avatar)
            }
            encodeImage = null
            dialog.dismiss()
        }
        dialogBinding.textCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.layoutAvatar.setOnClickListener {
            showPopup(it)
        }
        dialog.show()
    }

    private fun isMember(members: List<User>?, id: String): Boolean {
        members?.forEach {
            if (it.userId.equals(id)) return true
        }
        return false
    }

    private fun showDialogAddMember(group: Group) {
        var adapterMember: FriendAdapter? = null
        var adapterMemberSelect: AddFriendToNewGroupAdapter? = null
        var members = mutableListOf<User>()
        var memberSelects = mutableListOf<User>()

        adapterMember = FriendAdapter({
            if (!memberSelects.contains(it)) {
                memberSelects.add(it)
                members.remove(it)
                adapterMember?.notifyDataSetChanged()
                adapterMemberSelect?.notifyDataSetChanged()
            }
        })
        adapterMemberSelect = AddFriendToNewGroupAdapter({
            if (!members.contains(it)) {
                members.add(it)
                memberSelects.remove(it)
                adapterMember?.notifyDataSetChanged()
                adapterMemberSelect?.notifyDataSetChanged()
            }
        })

        val dialog = Dialog(this, R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR)
        dialog.setTitle(resources.getString(com.dinhtai.fchat.R.string.lable_member))
        dialog.setCancelable(true)
        var dialogBinding = DialogAddMemberBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        adapterMember.setData(members)
        adapterMemberSelect.setData(memberSelects)
        dialogBinding.buttonDone.setOnClickListener { }
        viewModelFriend.friends.observe(this, { friends ->
            members.addAll(friends.filter { !isMember(group.members, it.userId) })
            adapterMember.notifyDataSetChanged()
        })
        dialogBinding.recyclerFriend.adapter = adapterMember
        dialogBinding.recyclerMember.adapter = adapterMemberSelect
        dialogBinding.buttonDone.setOnClickListener {
            InfoYourself.token?.let { token ->
                memberSelects.forEach {
                    viewModel.addMemberToGroup(token, it.userId, group.id)
                }
                sendKeyWebsocket(memberSelects)
            }
            recreate()
        }
        dialogBinding.materialToolbar.setNavigationOnClickListener {
            recreate()
        }
        dialog.show()
    }

    private fun sendKeyWebsocket(members: List<User>) {
        mWebSocket?.let {
            var fileExternal = FileExternal(applicationContext)
            if (fileExternal.readFileAES("group" + groupId) == null || fileExternal.readFileAES(
                    "group" + groupId
                )!!.key.isNullOrEmpty()
            ) {
                var key = AESCrypt().generateKey
                fileExternal.writeFileAES(
                    Gson().toJson(
                        KeyAES(
                            "group" + groupId,
                            key,
                            Date().toString()
                        )
                    ), "group" + groupId
                )
                members.forEach { u ->
                    var pubKey = RSACrypt().covertPublicKey(u.publicKey!!);
                    send(
                        sendKey(
                            RSACrypt().getEncryptedString(pubKey, key) ?: "",
                            "create_key_secret",
                            groupId,
                            u.userId
                        )
                    )
                }

            }
        }
    }

    private fun sendKey(
        content: String,
        typeMessege: String,
        groupId: Int,
        receiverId: String
    ): String {
        var json = JsonObject()
        json.addProperty("group_id", groupId)
        json.addProperty("type_chat", "user")
        json.addProperty("token", InfoYourself.token)
        json.addProperty("type_data", typeMessege)
        json.addProperty("content", content)
        json.addProperty("receiver_id", receiverId)
        return json.toString()
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(com.dinhtai.fchat.R.menu.menu_choose_image_avatar)
        popup.setOnMenuItemClickListener({ item: MenuItem? ->
            when (item!!.itemId) {
                com.dinhtai.fchat.R.id.itemLibrary -> {
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
            .setAspectRatio(4, 4)
            .start(this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                encodeImage = encodeImage(resultUri)
                encodeImage?.let { Log.d("ddddd", it) }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun send(data: String) = runBlocking {
        Log.d("xxxxxxxxxx", data)
        mWebSocket?.send(data)
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
            this@ChatGroupSettingActivity?.runOnUiThread(Runnable {
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
