package com.dinhtai.fchat.ui.friend

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.os.Handler
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BindingFragment
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.databinding.FragmentFriendBinding
import com.dinhtai.fchat.network.NetworkConnection
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.ui.baseui.adapter.FollowAdapter
import com.dinhtai.fchat.ui.baseui.adapter.FriendAdapter
import com.dinhtai.fchat.ui.baseui.dialog.DetailFollowerDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailFriendDialog
import com.dinhtai.fchat.ui.baseui.dialog.DialogBottomSheetShowImage
import com.dinhtai.fchat.utils.config.ConfigAPI
import com.dinhtai.fchat.utils.config.ConfigMessage
import com.google.gson.JsonObject
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class FriendFragment : BindingFragment<FragmentFriendBinding>(),
    SwipeRefreshLayout.OnRefreshListener {
    override fun getLayoutResId(): Int = R.layout.fragment_friend
    lateinit var controller: NavController
    override val viewModel: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private lateinit var networkConnection: NetworkConnection
    private lateinit var adapterFriend: FriendAdapter
    private lateinit var adapterFollow: FollowAdapter
    private var mWebSocket: WebSocket? = null
    override fun setupView() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.friendVM = viewModel
        adapterFollow = FollowAdapter(::onItemClickFollow, ::yesItemClick, ::noItemClick)
        adapterFriend = FriendAdapter(::onItemClickFriend, ::showMenuPopupFriend, true)
        binding.recyclerInvitations.adapter = adapterFollow
        binding.recyclerFriends.adapter = adapterFriend
        networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(this, { isConnect ->
            if (isConnect) {
                WebSocketClient().connectSocket(ConfigAPI.WS_URL_CHAT, SocketListener())
            } else {
                showToast("Đã có lỗi, k kết nối được internet")
            }
        })
        controller = findNavController()
        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    //Show dialog to accept follow
    private fun onItemClickFollow(item: User) {
        item?.let {
            DetailFollowerDialog(item, this, requireContext(), {
                InfoYourself.token?.let { token ->
                    viewModel.acceptFollow(
                        token,
                        it.userId,
                        { refresh() })
                }
            }, {
                InfoYourself.token?.let { token ->
                    viewModel.refuseFollow(token, it.userId, { refresh() })
                }
            }).show()
        }
    }

    private fun refresh() {
        val timer = object : CountDownTimer(500, 500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                viewModel.getFriends(InfoYourself.token!!)
                viewModel.getFollows(InfoYourself.token!!)
            }
        }
        timer.start()
    }

    //Open new room chat
    private fun onItemClickFriend(item: User) {
        controller.navigate(FriendFragmentDirections.actionFriendFragmentToChatActivity(-1, item))
    }

    @SuppressLint("RestrictedApi")
    private fun showMenuPopupFriend(user: User, view: View) {
        val menuBuilder = MenuBuilder(requireContext())
        val inflater = MenuInflater(requireContext())
        inflater.inflate(R.menu.menu_popup_friend, menuBuilder)
        val optionsMenu = MenuPopupHelper(requireContext(), menuBuilder, view)
        optionsMenu.setForceShowIcon(true)
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                when (item!!.itemId) {
                    R.id.itemChat -> {
                        controller.navigate(
                            FriendFragmentDirections.actionFriendFragmentToChatActivity(
                                -1,
                                user
                            )
                        )
                    }
                    R.id.itemUnFriend -> {
                        viewModel.unFriend(InfoYourself.token!!, user.userId, { status ->
                            if (status.status == 1) {
                                showToast("Đã hủy kết bạn")
                                refresh()
                            } else {
                                showToast("Đã có lỗi xảy ra")
                            }
                        })
                    }
                    else -> false
                }
                return true
            }

            override fun onMenuModeChange(menu: MenuBuilder) {}
        })
        optionsMenu.show()
    }

    private fun yesItemClick(item: User) {
        if (InfoYourself.token != null) {
            viewModel.acceptFollow(InfoYourself.token!!, item.userId, {
                refresh()
            })
            showToast(resources.getString(R.string.lable_accept_follow) + " " + item.fullname)
        }
    }

    private fun noItemClick(item: User) {
        if (InfoYourself.token != null) {
            viewModel.refuseFollow(
                InfoYourself.token!!,
                item.userId,
                { viewModel.getFollows(InfoYourself.token!!) })
            showToast(resources.getString(R.string.lable_refuse_follow) + " " + item.fullname)
        }
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
            activity?.runOnUiThread(Runnable {
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

    override fun onRefresh() {
        viewModel.getFriends(InfoYourself.token!!)
        viewModel.getFollows(InfoYourself.token!!)
        Handler().postDelayed(object : Runnable {
            override fun run() {
                binding.swipeRefreshLayout.isRefreshing = (false)
            }

        }, 1500)
    }
}
