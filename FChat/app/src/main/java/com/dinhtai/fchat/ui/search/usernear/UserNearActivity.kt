package com.dinhtai.fchat.ui.search.usernear

import android.location.Location
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.databinding.ActivityUserNearBinding
import com.dinhtai.fchat.ui.baseui.adapter.FriendAdapter
import com.dinhtai.fchat.ui.baseui.dialog.DetailFollowedDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailFollowerDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailFriendDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailUserDialog
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.ui.profile.qrcode.QrCodeViewModel
import com.dinhtai.fchat.utils.checkGPSPermission
import com.dinhtai.fchat.utils.hide
import com.dinhtai.fchat.utils.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class UserNearActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var bindding: ActivityUserNearBinding
    val PERMISSION_ID = 1010
    private val viewModel: UserNearViewModel by lazy {
        ViewModelProvider(this)[UserNearViewModel::class.java]
    }
    private val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private val viewModelQr: QrCodeViewModel by lazy {
        ViewModelProvider(this)[QrCodeViewModel::class.java]
    }
    private var myLocation: (com.dinhtai.fchat.data.local.Location)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindding = ActivityUserNearBinding.inflate(layoutInflater)
        setContentView(bindding.root)
        bindding.lifecycleOwner = this
        bindding.vm = viewModel
        bindding.recyclerUser.adapter = FriendAdapter({ user ->
            InfoYourself.token?.let { token ->
                viewModelFriend.checkFriendOrFollower(token, user.userId, {
                    if (it == 1) {
                        DetailFriendDialog(
                            user,
                            this,
                            this@UserNearActivity,
                            {
                                viewModelFriend.unFriend(InfoYourself.token!!, user.userId)
                                refresh()
                            }).show()
                    } else if (it == 0) {
                        DetailUserDialog(user, this, this@UserNearActivity, {
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
                        DetailFollowerDialog(user, this, this@UserNearActivity, {
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
                            this@UserNearActivity,
                            {
                                viewModelFriend.cancelFollow(InfoYourself.token!!, user.userId)
                                refresh()
                            }).show()
                    }
                })
            }
        })
        bindding.swipeRefreshLayout.setOnRefreshListener(this)
        viewModel.users.observe(this, {
            if (it != null) {
                bindding.progressBar.hide()
            }
        })
        bindding.materialToolbar.setOnClickListener { finish() }
        hanlderLocation()
    }

    private fun hanlderLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (!checkGPSPermission(PERMISSION_ID)) {
            finish()
        } else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    myLocation =
                        com.dinhtai.fchat.data.local.Location(location.latitude, location.longitude)
                    viewModel.setLocation(InfoYourself.token!!, myLocation!!)
                    viewModel.getAddress(myLocation!!, applicationContext)
                    viewModel.getUsersNearByLocation(myLocation!!, 10.0)
                }
            }
        }
    }

    private fun refresh() {
        viewModelFriend.getFollows(InfoYourself.token!!)
        viewModelFriend.getFriends(InfoYourself.token!!)
        myLocation?.let {
            viewModel.setLocation(InfoYourself.token!!, myLocation!!)
            viewModel.getAddress(myLocation!!, applicationContext)
            viewModel.getUsersNearByLocation(myLocation!!, 10.0)
        }
    }

    override fun onRefresh() {
        refresh()
        Handler().postDelayed(object : Runnable {
            override fun run() {
                bindding.swipeRefreshLayout.isRefreshing = (false)
            }

        }, 2000)
    }
}
