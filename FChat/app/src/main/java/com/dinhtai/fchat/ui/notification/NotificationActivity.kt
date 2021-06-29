package com.dinhtai.fchat.ui.notification

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.Notification
import com.dinhtai.fchat.databinding.ActivityNotificationBinding
import com.dinhtai.fchat.ui.baseui.adapter.NotificationAdapter
import com.dinhtai.fchat.ui.baseui.dialog.DetailFollowedDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailFollowerDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailFriendDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailUserDialog
import com.dinhtai.fchat.ui.chat.ChatActivity
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.ui.profile.qrcode.QrCodeViewModel
import com.dinhtai.fchat.utils.config.mapType
import com.dinhtai.fchat.utils.toast
import com.mpt.android.stv.Slice

class NotificationActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var adapter: NotificationAdapter
    private val viewModel: NotificationViewModel by lazy {
        ViewModelProvider(this)[NotificationViewModel::class.java]
    }
    private val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private val viewModelQr: QrCodeViewModel by lazy {
        ViewModelProvider(this)[QrCodeViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        refresh()
        viewModel.status.observe(this,{
            refresh()
        })
        binding.materialToolbar.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.itemTickAll -> {
                     viewModel.updateAllStatusNotification(InfoYourself.token!!)
                }
                R.id.itemDeleteAll ->{
                    viewModel.deleteAllNotification(InfoYourself.token!!)
                }
            }
            true
        }
        binding.materialToolbar.setNavigationOnClickListener { finish() }
        adapter = NotificationAdapter(::onItemClick, ::onMoreClick)
        binding.recyclerNotification.adapter = adapter
        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun onMoreClick(notification: Notification, view: View) {
        var popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_more)
        popup.setOnMenuItemClickListener({ item: MenuItem? ->
            when (item!!.itemId) {
                R.id.itemDelete -> {
                    viewModel.deleteNotification(InfoYourself.token!!,notification.id)
                }
            }
            true
        })
        popup.show()
    }

    private fun onItemClick(notification: Notification) {
        var token = InfoYourself.token!!
        var user = notification.sender!!
        viewModel.updateStatusNotification(InfoYourself.token!!,notification.id)
        when (notification.type) {
            "2", "4", "5","6" -> {
                viewModelFriend.checkFriendOrFollower(token, user.userId, {
                    if (it == 1) {
                        DetailFriendDialog(
                            user,
                            this,
                            this,
                            {
                                viewModelFriend.unFriend(InfoYourself.token!!, user.userId)
                                refresh()
                            }).show()
                    } else if (it == 0) {
                        DetailUserDialog(user, this, this, {
                            InfoYourself.token?.let { token ->
                                viewModelQr.setFollow(
                                    token,
                                    user.userId,
                                    null,
                                    { status ->
                                        toast("Đã gửi thành công")
                                        refresh()
                                    })
                            }
                        }, {
                        }).show()
                    } else if (it == 2) {
                        DetailFollowerDialog(user, this, this, {
                            InfoYourself.token?.let { token ->
                                viewModelFriend.acceptFollow(
                                    token,
                                    user.userId, { refresh() }
                                )
                            }
                        }, {
                            InfoYourself.token?.let { token ->
                                viewModelFriend.refuseFollow(
                                    token,
                                    user.userId, { refresh() }
                                )
                            }
                        }).show()
                    } else if (it ==3){
                        DetailFollowedDialog(
                            user,
                            this,
                            this,
                            {
                                viewModelFriend.cancelFollow(InfoYourself.token!!, user.userId)
                                refresh()
                            }).show()
                    }
                })
            }
            "1", "3", "7" -> {
                var intent = Intent(this,ChatActivity::class.java)
                intent.putExtra("groupId",notification.group!!.id)
                startActivity(intent)
            }
        }
    }

    private fun refresh() {
        viewModelFriend.getFollows(InfoYourself.token!!)
        viewModelFriend.getFriends(InfoYourself.token!!)
        viewModel.getNotificationById(InfoYourself.token!!)
    }
    
    override fun onRefresh() {
        refresh()
        Handler().postDelayed(object : Runnable {
            override fun run() {
                binding.swipeRefreshLayout.isRefreshing = (false)
            }

        }, 1500)
    }
}
