package com.dinhtai.fchat.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dinhtai.fchat.R
import com.dinhtai.fchat.ui.search.SearchActivity
import com.dinhtai.fchat.base.BindingFragment
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.Message
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.FragmentMessageBinding
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.ui.baseui.adapter.FriendOnlineAdapter
import com.dinhtai.fchat.ui.baseui.adapter.MessageShortAdapter
import com.dinhtai.fchat.ui.baseui.dialog.DetailMyInfoDialog
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.ui.main.ActivityMainViewModel
import com.dinhtai.fchat.utils.config.ConfigAPI
import com.dinhtai.fchat.utils.config.ConfigMessage
import com.dinhtai.fchat.utils.prepareFilePart
import com.google.gson.JsonObject
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MessageFragment : BindingFragment<FragmentMessageBinding>() {
    override fun getLayoutResId(): Int = R.layout.fragment_message
    lateinit var controller: NavController
    override val viewModel: MessageViewModel by lazy {
        ViewModelProvider(this)[MessageViewModel::class.java]
    }
    private val viewModelMain: ActivityMainViewModel by lazy {
        ViewModelProvider(this)[ActivityMainViewModel::class.java]
    }
    private val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private lateinit var dialogInfo:DetailMyInfoDialog
    private var mWebSocket: WebSocket? = null
    override fun setupView() {
        binding.lifecycleOwner = viewLifecycleOwner
        WebSocketClient().connectSocket(ConfigAPI.WS_URL_CHAT, SocketListener())
        binding.recyclerShortMsg.adapter = MessageShortAdapter(::onItemClick)
        binding.msgVM = viewModel
        binding.vmMain =viewModelMain
        binding.vmFriend = viewModelFriend
        binding.recyclerFriendOnline.adapter = FriendOnlineAdapter(::onItemClick)
        binding.linearSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
        dialogInfo =  DetailMyInfoDialog(viewLifecycleOwner,
            requireActivity(), viewModelMain, {
                requestPermionAndPickImage()
            })
        binding.imageAvatar.setOnClickListener {
            dialogInfo.show()
        }
        controller = findNavController()

    }

    //Yêu cầu quyền truy câp
    private fun requestPermionAndPickImage() {
        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                startCropImageActivity()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
            }
        }
        TedPermission.with(requireContext())
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("Nếu bạn từ chối quyền, bạn không thể sử dụng dịch vụ này\n\n làm ơn hãy chấp nhận bằng cách [Setting] > [Permission]")
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
            .start(requireContext(), this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val resultUri: Uri = result.uri

                //encodeImage = requireContext().encodeImage(resultUri)
                resultUri?.let {
                    viewModelMain.updateAvatarUser(InfoYourself.token!!,requireContext().prepareFilePart(it,"avatar")) }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    override fun onResume() {
        super.onResume()
        InfoYourself.token?.let {
            viewModel.loadShortMessage(it)
        }

    }

    private fun onItemClick(item: Message) {
        item.user?.let {
            val action = MessageFragmentDirections.actionFragmentMessageToChatActivity(user = it)
            controller.navigate(action)
        }
    }

    private fun onItemClick(item: User) {
        item?.let {
            val action = MessageFragmentDirections.actionFragmentMessageToChatActivity(user = it)
            controller.navigate(action)
        }
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
            requireActivity().runOnUiThread(Runnable {
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
            InfoYourself.token?.let {
                viewModel.loadShortMessage(it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mWebSocket?.cancel()
        mWebSocket?.close(1000, "byte")
    }
}
