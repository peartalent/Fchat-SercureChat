package com.dinhtai.fchat.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navArgs
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.databinding.ActivityChatBinding
import com.dinhtai.fchat.databinding.DialogSelectMemberCallBinding
import com.dinhtai.fchat.network.NetworkConnection
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.ui.baseui.adapter.AddFriendToNewGroupAdapter
import com.dinhtai.fchat.ui.baseui.adapter.ChatAdapter
import com.dinhtai.fchat.ui.baseui.adapter.ChatGroupAdapter
import com.dinhtai.fchat.ui.baseui.adapter.MemberGroupAdapter
import com.dinhtai.fchat.ui.baseui.dialog.DialogBottomSheetShowImage
import com.dinhtai.fchat.ui.call.ActivityOutingCall
import com.dinhtai.fchat.ui.call.Constant
import com.dinhtai.fchat.ui.chat.setting.group.ChatGroupSettingActivity
import com.dinhtai.fchat.ui.baseui.dialog.RecordDialog
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.ui.group.GroupViewModel
import com.dinhtai.fchat.ui.home.MessageViewModel
import com.dinhtai.fchat.ui.image.ImageActivity
import com.dinhtai.fchat.utils.*
import com.dinhtai.fchat.utils.config.ConfigAPI
import com.dinhtai.fchat.utils.config.ConfigMessage
import com.dinhtai.fchat.utils.file.FileExternal
import com.dinhtai.fchat.utils.safe.AESCrypt
import com.dinhtai.fchat.utils.safe.RSACrypt
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.File
import java.io.Serializable
import java.sql.Timestamp
import java.util.*
import kotlin.coroutines.CoroutineContext

class ChatActivity : AppCompatActivity(), CoroutineScope, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: ActivityChatBinding
    private var dialog: RecordDialog? = null
    private val PICK_IMAGE_REQUEST = 1
    private val READ_EXTERNAL_REQUEST = 2
    private val viewModel: ChatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }
    private val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private val viewModelMessageShort: MessageViewModel by lazy {
        ViewModelProvider(this)[MessageViewModel::class.java]
    }
    private val viewModelGroup: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private val fileExternal: FileExternal by lazy { FileExternal(this) }
    private var mWebSocket: WebSocket? = null
    private var user: User? = null
    private var groupId = -1
    private var encodeImage: String? = null
    private var adapterChatUser: ChatAdapter? = null
    private var adapterChatGroup: ChatGroupAdapter? = null
    private lateinit var networkConnection: NetworkConnection
    private lateinit var emojiPopupMenu: EmojiPopup
    private lateinit var inputManager: InputMethodManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.chatVM = viewModel
        binding.recyclerChat.setHasFixedSize(false)
        binding.recyclerChat.setItemViewCacheSize(20)
        val args: ChatActivityArgs by navArgs()
        args.user?.let {
            user = it
            binding.user = user
            InfoYourself.token?.let { token ->
                viewModel.getMessagesById(token, it.userId)
            }
            Log.d("xxxxxxxxxxxxxxxxxxxxxxxxx", user.toString())
            fileExternal.readFileAES(user!!.userId)?.let {
                adapterChatUser =
                    ChatAdapter(it, ::onItemClick, { v, m -> showPopup(v, m.msgId!!) }, {
                        showDialogSendKey(it, "user")
                    })
                binding.recyclerChat.adapter = adapterChatUser

            }
        }
        intent.getSerializableExtra("user")?.let { user = it as User }
//        groupId =intent.getIntExtra("groupId",-1)
        args.groupId?.let {
            groupId = it
            if (groupId != -1) {
                InfoYourself.token?.let { token ->
                    viewModel.getMessageGroupById(token, it)
                }
                fileExternal.readFileAES("group" + groupId)?.let {
                    adapterChatGroup = ChatGroupAdapter(
                        fileExternal.readFileAES("group" + groupId)!!,
                        ::onItemClick,
                        { v, m -> showPopup(v, m.msgId!!) },
                        {
                            showDialogSendKey(it, "group")
                        })
                    binding.recyclerChat.adapter = adapterChatGroup
                }

            }
        }
        inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//        EmojiManager.install(TwitterEmojiProvider())
//        EmojiManager.install(FacebookEmojiProvider())
        EmojiManager.install(GoogleEmojiProvider())
        emojiPopupMenu = EmojiPopup.Builder.fromRootView(binding.root).build(editInputText);
        networkConnection = NetworkConnection(this)
        networkConnection.observe(this, { isConnect ->
            if (isConnect) {
                WebSocketClient().connectSocket(ConfigAPI.WS_URL_CHAT, SocketListener())
                viewModel.countMessages().observe(this, {
                    if (it > 1) {
                        Log.d("scroll view ", it.toString())
                        binding.recyclerChat.smoothScrollToPosition(it - 1)
                    }
                })
            } else toast("Disconnection internet")
        })

        binding.materialToolbar.setNavigationOnClickListener {
            finish()
        }
        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun onResume() {
        super.onResume()
        WebSocketClient().connectSocket(ConfigAPI.WS_URL_CHAT, SocketListener())
        viewModel.countMessages().observe(this, {
            if (it > 1) {
                Log.d("scroll view ", it.toString())
                binding.recyclerChat.smoothScrollToPosition(it - 1)
            }
        })
    }

    private fun onItemClick(item: Message) {
        Log.d("click item message", item.toString())
        currentFocus?.let {
            inputManager.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
        if (item.typeMessege == TypeMessege.img) {
            viewModel.messageImages.value?.forEachIndexed { index, message ->
                if (item.msgId == message.msgId) {
                    var i = index
                    var intent = Intent(this, ImageActivity::class.java)
                    Log.d("item index", i.toString())
                    intent.putExtra("index", i)
                    if (groupId != -1) {
                        intent.putExtra("type", "group")
                        intent.putExtra("id", groupId)
                    } else {
                        intent.putExtra("type", "user")
                        intent.putExtra("idUser", user?.userId)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    var isOpened = false

    fun setListenerToRootView() {
        val activityRootView: View = window.decorView.findViewById(android.R.id.content)
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = activityRootView.rootView.height - activityRootView.height
            if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
                if (isOpened == false) {
                    // key dang mo
                }
                isOpened = true
            } else if (isOpened == true) {
                if (emojiPopupMenu.isShowing) {
                    emojiPopupMenu.dismiss()
                    inputManager.hideSoftInputFromWindow(
                        currentFocus!!.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                    inputManager.showSoftInput(editInputText, InputMethodManager.SHOW_IMPLICIT);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

                }
                isOpened = false
            }
        }
    }

    private fun eventListener() {
        binding.imageEmoji.setOnClickListener {
            emojiPopupMenu.toggle()
            if (emojiPopupMenu.isShowing) binding.imageEmoji.setImageResource(R.drawable.ic_cancel)
            else binding.imageEmoji.setImageResource(R.drawable.ic_chat)
        }
        editInputText.doOnTextChanged { text, start, before, count ->
            text?.let {
                if (text.trim().length > 0) {
                    hintChatTextFoucus()
                    imageSend.visibility = View.VISIBLE
//                    gui cai typing o day
                } else {
                    visibilityChatTextFoucus()
                    imageSend.visibility = View.GONE
                }
            }
        }
//        send
        binding.root.imageSend.setOnClickListener {
            networkConnection.observe(this, { isConnect ->
                if (isConnect) {
                    if (!editInputText.text.isEmpty()) {
                        if (user != null || groupId == -1) {
                            send(
                                messageJsonSendUser(
                                    editInputText.text.toString().trim(),
                                    TypeMessege.text,
                                    user!!.userId
                                )
                            )

                        } else {
                            send(
                                messageJsonSendGroup(
                                    editInputText.text.toString().trim(),
                                    TypeMessege.text,
                                    groupId
                                )
                            )
                        }
                        editInputText.text = null
                    }
                } else {
                    toast("Đã có lỗi, k kết nối được internet")
                }
            })

        }

        binding.imageMic.setOnClickListener {
            dialog = RecordDialog(this, this,
                {
                    if (groupId == -1) {
                        send(
                            messageJsonSendUser(
                                encodeFile(recordFile),
                                TypeMessege.audio,
                                user!!.userId
                            )
                        )
                    } else {
                        send(
                            messageJsonSendGroup(
                                encodeFile(recordFile),
                                TypeMessege.audio,
                                groupId
                            )
                        )
                    }
                }
            )
            dialog?.show()
            var window = dialog?.window
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        binding.imageCamera.setOnClickListener {
            requestPermionAndPickImage()
        }

        binding.imageVideo.setOnClickListener {
            val intent = Intent(applicationContext, ActivityOutingCall::class.java)
            intent.putExtra("group", groupId)
            if (groupId != -1) {
                viewModel.group.observe(this, {
                    it?.let {
                        it.members?.let { it1 ->
                            showDialogSelectCallGroup(
                                it1,
                                Constant.REMOTE_MSG_VALUE_TYPE_VIDEO
                            )
                        }
                    }
                })
            } else {
                intent.putExtra("user", user)
                var users = listOf<User>(user!!)
                intent.putExtra("members", users as Serializable)
                intent.putExtra("type", Constant.REMOTE_MSG_VALUE_TYPE_VIDEO)
                startActivity(intent)
            }

        }
        binding.imageCall.setOnClickListener {
            val intent = Intent(applicationContext, ActivityOutingCall::class.java)
            intent.putExtra("group", groupId)
            if (groupId != -1) {
                viewModel.group.observe(this, {
                    it?.let {
                        it.members?.let { it1 ->
                            showDialogSelectCallGroup(
                                it1,
                                Constant.REMOTE_MSG_VALUE_TYPE_AUDIO
                            )
                        }
                    }
                })
            } else {
                intent.putExtra("user", user)
                var users = listOf<User>(user!!)
                intent.putExtra("members", users as Serializable)
                intent.putExtra("type", Constant.REMOTE_MSG_VALUE_TYPE_AUDIO)
                startActivity(intent)
            }
        }

        binding.imageInfo.setOnClickListener {
            if (groupId != -1) {
                val intent = Intent(applicationContext, ChatGroupSettingActivity::class.java)
                intent.putExtra("groupId", groupId)
                startActivity(intent)
            } else {
                showMenuPopupChatSettingFriend(it)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showMenuPopupChatSettingFriend(view: View) {
        val menuBuilder = MenuBuilder(this)
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.menu_setting_chat_friend, menuBuilder)
        val optionsMenu = MenuPopupHelper(this, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                when (item!!.itemId) {
                    R.id.itemDelete -> {
                        send(sendDeleteAllMessage(user!!.userId, "user"))
                        true
                    }
                    R.id.itemCancelFriend -> {
                        viewModelFriend.unFriend(InfoYourself.token!!, user!!.userId)
                        finish()
                        true
                    }
                    R.id.itemShowImage -> {
                        if (groupId != -1){
                            viewModel._messageImages.value?.let {
                                DialogBottomSheetShowImage(
                                    it, fileExternal.readFileAES("group" + groupId)!!,
                                    this@ChatActivity,
                                    ::listenerBottomSheet
                                ).show(supportFragmentManager, null)
                            }
                        } else {
                            viewModel._messageImages.value?.let {
                                DialogBottomSheetShowImage(
                                    it, fileExternal.readFileAES(user!!.userId)!!,
                                    this@ChatActivity,
                                    ::listenerBottomSheet
                                ).show(supportFragmentManager, null)
                            }
                        }

                    }
                    else -> false
                }
                return true
            }

            override fun onMenuModeChange(menu: MenuBuilder) {}
        })
        optionsMenu.show()
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
                } else {
                    intent.putExtra("type", "user")
                    intent.putExtra("idUser", user?.userId)
                }
                startActivity(intent)
            }
        }
    }

    //Yêu cầu quyền truy câp
    private fun requestPermionAndPickImage() {
        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
//                thành công thì hiển thị ds hình ảnh
                onpenBottomPickerImage()
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

    private fun onpenBottomPickerImage() {
        TedBottomPicker.with(this)
            .setPeekHeight(1000)
            .setCompleteButtonText(resources.getString(R.string.lable_sent))
            .setSelectMaxCount(4)
            .showMultiImage {
                it.forEach { uriImage ->
                    encodeImage = encodeImage(uriImage)
                    encodeImage?.let { it1 ->
                        if (groupId == -1) {
                            send(
                                messageJsonSendUser(
                                    it1,
                                    TypeMessege.img,
                                    user!!.userId
                                )
                            )
                        } else {
                            send(messageJsonSendGroup(it1, TypeMessege.img, groupId))
                        }

                    }
                }
            }
    }

    fun encodeFile(attachment: File): String {
        return Base64.encodeToString(attachment.readBytes(), Base64.NO_WRAP)
    }

    fun decode(imageString: String) {
        // Decode base64 string to image
        val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//        imageview.setImageBitmap(decodedImage)
    }

    private fun hintChatTextFoucus() {
        imageMic.visibility = View.GONE
        imageCamera.visibility = View.GONE
    }

    private fun visibilityChatTextFoucus() {
        imageEmoji.visibility = View.VISIBLE
        imageMic.visibility = View.VISIBLE
        imageCamera.visibility = View.VISIBLE
    }

    private fun send(data: String) = runBlocking {
        Log.d("sen gì đó", data)
        mWebSocket?.send(data)
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            this@ChatActivity.runOnUiThread(Runnable {
                mWebSocket = webSocket
                var json = JsonObject()
                InfoYourself.token?.let { token ->
                    json.addProperty("token", token)
                    json.addProperty("type_data", ConfigMessage.USER_LOGIN)
                    webSocket.send(json.toString())
                }
                eventListener()
                if (groupId != -1) {
                    if (fileExternal.readFileAES("group" + groupId) == null || fileExternal.readFileAES(
                            "group" + groupId
                        )!!.key.isNullOrEmpty()
                    ) {
                        val builder = AlertDialog.Builder(this@ChatActivity)
                        builder.apply {
                            setTitle("Yêu cầu key để vào chat")
                            setMessage("Bạn hiện đang bị mất key bảo mật tin nhắn, bạn có muốn nhờ các thành viên trong group giúp bạn nhận lại key")
                            setCancelable(false)
                            setPositiveButton(android.R.string.yes) { dialog, which ->
                                var json = JsonObject()
                                var gson = Gson()
                                var message = Message(
                                    null,
                                    "keyRequest",
                                    TypeMessege.other,
                                    Timestamp(Date().time),
                                    null,
                                    isSender = true,
                                    null,
                                    receiverId = groupId.toString(), senderId = null
                                )
                                val jsonMsg = gson.toJson(message)
                                json.addProperty("type_chat", "group")
                                json.addProperty("token", InfoYourself.token)
                                json.addProperty("type_data", ConfigMessage.USER_SEND_MESSAGE)
                                json.addProperty("message", jsonMsg)
                                webSocket.send(json.toString())
                                toast("Đã gửi")
                                dialog.dismiss()
                                this@ChatActivity.finish()
                            }
                            setNegativeButton(android.R.string.no) { dialog, which ->
                                toast("Đã hủy")
                                dialog.dismiss()
                                this@ChatActivity.finish()
                            }

                        }
                        viewModel.isAdmin.observe(this@ChatActivity, {
                            if (it) {
                                builder.setNeutralButton("Tạo mới", { dialog, which ->
                                    viewModel.group.observe(this@ChatActivity, {
                                        it?.let {
                                            refeshKeyGroup(groupId, it.members)
                                            dialog.dismiss()
                                            recreate()
                                        }
                                    })
                                })
                                builder.show()
                            } else {
                                builder.show()
                            }
                        })
                        builder.show()
                    }
                } else {
                    var key = fileExternal.readFileAES(user!!.userId)
                    if (key == null || key.key.isNullOrEmpty()) {
                        val builder = AlertDialog.Builder(this@ChatActivity)
                        builder.apply {
                            setTitle("Bạn đã đánh key bảo mật")
                            setMessage("Bạn hiện đang bị mất key bảo mật tin nhắn, bạn có muốn tạo lại key mới hay nhờ bạn bè chia sẻ lại key")
                            setCancelable(false)
                            setPositiveButton("Tạo mới") { dialog, which ->
                                var keyString = AESCrypt().generateKey
                                fileExternal.writeFileAES(
                                    Gson().toJson(
                                        KeyAES(
                                            user!!.userId,
                                            keyString,
                                            Date().toString()
                                        )
                                    ), user!!.userId
                                )
                                var pubKey = RSACrypt().covertPublicKey(user!!.publicKey!!);
                                mWebSocket?.send(
                                    sendKeyUser(
                                        RSACrypt().getEncryptedString(pubKey, keyString) ?: "",
                                        "create_key_secret",
                                        user!!.userId
                                    )
                                )
                                send(sendDeleteAllMessage(user!!.userId, "user"))
                                toast("Đã tạo mới")
                                var i = Intent(this@ChatActivity,ChatActivity::class.java)
                                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                i.putExtra("user",user)
                                startActivity(i)
                           //     dialog.dismiss()
                            }
                            setNeutralButton("Yêu cầu") { dialog, which ->
                                var json = JsonObject()
                                var gson = Gson()
                                var message = Message(
                                    null,
                                    "keyRequest",
                                    TypeMessege.other,
                                    Timestamp(Date().time),
                                    null,
                                    isSender = true,
                                    null,
                                    receiverId = user!!.userId, senderId = null
                                )
                                val jsonMsg = gson.toJson(message)
                                json.addProperty("type_chat", "user")
                                json.addProperty("token", InfoYourself.token)
                                json.addProperty("type_data", ConfigMessage.USER_SEND_MESSAGE)
                                json.addProperty("message", jsonMsg)
                                webSocket.send(json.toString())
                                toast("Đã gửi")
                                dialog.dismiss()
                                this@ChatActivity.finish()
                            }
                            setNegativeButton(android.R.string.no) { dialog, which ->
                                toast("Đã hủy")
                                dialog.dismiss()
                                this@ChatActivity.finish()
                            }
                            show()
                        }
                    } else {
                       udStatusMessages(user!!.userId)
                    }
                }
            })

        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.d("close rồi hic", "code" + code)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d("closed rồi", code.toString())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            t.printStackTrace()
            Log.d("fail rồi", "code" + t.message)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.d("message socket chatactivity", text)
            refreshVM()
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            Log.d("message socket chatactivity bytr", bytes.toString())
            refreshVM()
        }
    }
    private fun udStatusMessages(userId:String){
        viewModel.updateStatusMessages(InfoYourself.token!!,userId)
    }
    fun refreshVM() {
        if (groupId == -1) {
            InfoYourself.token?.let { token ->
                viewModel.getMessagesById(token, user!!.userId)
                viewModelMessageShort.loadShortMessage(token)
            }
        } else {
            Log.d("message socket chatactivity", "jiji")
            InfoYourself.token?.let { token ->
                viewModel.getMessageGroupById(token, groupId)
                viewModelGroup.getAllGroup(token)
            }
        }
    }

    private fun refeshKeyGroup(groupId: Int, members: List<User>?) {
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
            members?.forEach { u ->
                var pubKey = RSACrypt().covertPublicKey(u.publicKey!!);
                send(
                    sendKeyMember(
                        RSACrypt().getEncryptedString(pubKey, key) ?: "",
                        "create_key_secret",
                        groupId,
                        u.userId
                    )
                )
            }

        }
    }

    private fun messageJsonSendUser(
        content: String,
        typeMessege: TypeMessege,
        receiverId: String
    ): String {
        var key = fileExternal.readFileAES(user!!.userId)
        var json = JsonObject()
        var gson = Gson()
        var message = Message(
            null,
            content,
            typeMessege,
            Timestamp(Date().time),
            null,
            isSender = true,
            null,
            receiverId = receiverId, senderId = null
        )
        if (typeMessege == TypeMessege.text || typeMessege == TypeMessege.img) {
            message = Message(
                null,
                AESCrypt().getEncryptedString(key!!.key, content)!!,
                typeMessege,
                Timestamp(Date().time),
                null,
                isSender = true,
                null,
                receiverId = receiverId, senderId = null
            )
        }
        val jsonMsg = gson.toJson(message)
        json.addProperty("type_chat", "user")
        json.addProperty("token", InfoYourself.token)
        json.addProperty("type_data", ConfigMessage.USER_SEND_MESSAGE)
        json.addProperty("message", jsonMsg)
        return json.toString()
    }

    private fun sendKeyUser(
        content: String,
        typeMessege: String,
        receiverId: String
    ): String {
        var json = JsonObject()
        json.addProperty("type_chat", "user")
        json.addProperty("token", InfoYourself.token)
        json.addProperty("type_data", typeMessege)
        json.addProperty("content", content)
        json.addProperty("receiver_id", receiverId)
        return json.toString()
    }

    private fun sendDeleteMessage(
        msgId: Int,
        receiverId: String,
        mode: String   // user or group
    ): String {
        var json = JsonObject()
        json.addProperty("type_chat", mode)
        json.addProperty("token", InfoYourself.token)
        json.addProperty("type_data", "delete")
        json.addProperty("msg_id", msgId)
        json.addProperty("receiver_id", receiverId)
        return json.toString()
    }

    private fun sendDeleteAllMessage(
        receiverId: String,
        mode: String   // user or group
    ): String {
        var json = JsonObject()
        json.addProperty("type_chat", mode)
        json.addProperty("token", InfoYourself.token)
        json.addProperty("type_data", "deleteAll")
        json.addProperty("receiver_id", receiverId)
        return json.toString()
    }

    private fun sendKeyMember(
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

    private fun messageJsonSendGroup(
        content: String,
        typeMessege: TypeMessege,
        groupReceiver: Int
    ): String {
        var key = fileExternal.readFileAES("group" + groupId)
        if (key == null || key.key.isNullOrEmpty()) {
            // sẽ gửi 1 thông báo dạng other, khi người khác click vào sẽ gửi key cho thằng bị mất key
            var json = JsonObject()
            var gson = Gson()
            var message = Message(
                null,
                "keyRequest",
                TypeMessege.other,
                Timestamp(Date().time),
                null,
                isSender = true,
                null,
                receiverId = groupReceiver.toString(), senderId = null
            )
            if (typeMessege == TypeMessege.text ) {
                message = Message(
                    null,
                    AESCrypt().getEncryptedString(key!!.key, content)!!,
                    typeMessege,
                    Timestamp(Date().time),
                    null,
                    isSender = true,
                    null,
                    receiverId = groupReceiver.toString(), senderId = null
                )
            }
            val jsonMsg = gson.toJson(message)
            json.addProperty("type_chat", "group")
            json.addProperty("token", InfoYourself.token)
            json.addProperty("type_data", ConfigMessage.USER_SEND_MESSAGE)
            json.addProperty("message", jsonMsg)
            return json.toString()
        } else {
            var json = JsonObject()
            var gson = Gson()
            var message = Message(
                null,
                content,
                typeMessege,
                Timestamp(Date().time),
                null,
                isSender = true,
                null,
                receiverId = groupReceiver.toString(), senderId = null
            )
            if (typeMessege == TypeMessege.text|| typeMessege == TypeMessege.img) {
                message = Message(
                    null,
                    AESCrypt().getEncryptedString(key!!.key, content)!!,
                    typeMessege,
                    Timestamp(Date().time),
                    null,
                    isSender = true,
                    null,
                    receiverId = groupReceiver.toString(), senderId = null
                )
            }
            val jsonMsg = gson.toJson(message)
            json.addProperty("type_chat", "group")
            json.addProperty("token", InfoYourself.token)
            json.addProperty("type_data", ConfigMessage.USER_SEND_MESSAGE)
            json.addProperty("message", jsonMsg)
            return json.toString()
        }
        return ""
    }

    private fun showDialogSelectCallGroup(users: List<User>, typeCall: String) {
        var adapterRaw: MemberGroupAdapter? = null
        var adapterMemberSelect: AddFriendToNewGroupAdapter? = null
        var memberSelects = mutableListOf<User>()
        var memberRaws = mutableListOf<User>()
        var members = users.filter { !it.userId.equals(InfoYourself.userID) }
        memberRaws.addAll(members)
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        adapterRaw = MemberGroupAdapter({
            if (!memberSelects.contains(it)) {
                memberSelects.add(it)
                memberRaws.remove(it)
                adapterRaw?.notifyDataSetChanged()
                adapterMemberSelect?.notifyDataSetChanged()
            }
        })
        adapterMemberSelect = AddFriendToNewGroupAdapter({
            if (!memberRaws.contains(it)) {
                memberRaws.add(it)
                memberSelects.remove(it)
                adapterRaw?.notifyDataSetChanged()
                adapterMemberSelect?.notifyDataSetChanged()
            }
        })
        adapterMemberSelect.setData(memberSelects)
        adapterRaw.setData(memberRaws)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        var dialogBinding = DialogSelectMemberCallBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.lifecycleOwner = this
        dialogBinding.recyclerMemberSelect.adapter = adapterMemberSelect
        dialogBinding.recyclerMemberRaw.adapter = adapterRaw
        dialogBinding.buttonSelectAll.setOnClickListener {
            memberSelects.removeAll { true }
            memberSelects.addAll(members)
            memberRaws.removeAll { true }
            adapterMemberSelect.notifyDataSetChanged()
            adapterRaw.notifyDataSetChanged()
        }
        dialogBinding.buttonCall.setOnClickListener {
            if (memberSelects.size > 0) {
                val intent = Intent(applicationContext, ActivityOutingCall::class.java)
                intent.putExtra("groupId", groupId)
                intent.putExtra("members", memberSelects as Serializable)
                intent.putExtra("type", typeCall)
                startActivity(intent)
                dialog.dismiss()
            } else {
                toast("Chưa chọn danh sách gọi")
            }
        }
        dialogBinding.materialToolbar.setNavigationOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
        //   mWebSocket?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        //     mWebSocket?.cancel()
    }

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1
    }

    // mode là user or group
    private fun showDialogSendKey(message: Message, mode: String) {
        // chú ý phân biệt group và client
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Chú ý")
            setMessage("Bạn có muốn gửi key mã hóa cho người này")
            setPositiveButton(android.R.string.yes) { dialog, which ->
                //       var key = fileExternal.readFileAES("group"+groupId)
                if (mode.equals("group")) {
                    var key = fileExternal.readFileAES("group" + groupId)
                    if (key != null && !key?.key.isNullOrEmpty()) {
                        Log.d("public key group member", message.user.toString())
                        var pubKey = RSACrypt().covertPublicKey(message.user!!.publicKey!!);
                        mWebSocket?.send(
                            // nên gửi theo 2 kiểu
                            sendKeyMember(
                                RSACrypt().getEncryptedString(pubKey, key.key) ?: "",
                                "create_key_secret",
                                groupId,
                                message.user!!.userId
                            )
                        )
                    }
                } else {
                    var key = fileExternal.readFileAES(user!!.userId)
                    if (key != null && !key?.key.isNullOrEmpty()) {
                        var pubKey = RSACrypt().covertPublicKey(user!!.publicKey!!);
                        mWebSocket?.send(
                            sendKeyUser(
                                RSACrypt().getEncryptedString(pubKey, key.key) ?: "",
                                "create_key_secret",
                                user!!.userId
                            )
                        )
                    }
                }
                toast("Đã gửi")
                dialog.dismiss()
            }
            setNegativeButton(android.R.string.no) { dialog, which ->
                toast("Đã hủy")
                dialog.dismiss()
            }
            show()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showPopup(view: View, idMessage: Int) {
        val popup = PopupMenu(this, view)
        //popup.inflate(R.menu.menu_delete_message)
        val menuBuilder = MenuBuilder(this)
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.menu_delete_message, menuBuilder)
        val optionsMenu = MenuPopupHelper(this, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                when (item!!.itemId) {
                    R.id.itemDelete -> {
                        if (groupId == -1) {
                            send(sendDeleteMessage(idMessage, user!!.userId, "user"))
                        } else {
                            send(sendDeleteMessage(idMessage, groupId.toString(), "group"))
                        }
                        true
                    }
                    else -> false
                }
                return true
            }

            override fun onMenuModeChange(menu: MenuBuilder) {}
        })
        optionsMenu.show()
        //  popup.show()
    }

    override fun onRefresh() {
        refreshVM()
        Handler().postDelayed(object : Runnable {
            override fun run() {
                binding.swipeRefreshLayout.isRefreshing = (false)
            }

        }, 2000)
    }
}
