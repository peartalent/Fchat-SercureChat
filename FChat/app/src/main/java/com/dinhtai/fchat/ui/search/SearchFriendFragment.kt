package com.dinhtai.fchat.ui.search

import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BindingFragment
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.databinding.FragmentSearchFriendBinding
import com.dinhtai.fchat.ui.baseui.adapter.FriendAdapter
import com.dinhtai.fchat.ui.baseui.dialog.*
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.ui.profile.qrcode.QrCodeViewModel
import com.dinhtai.fchat.utils.toast

class SearchFriendFragment(private var v: SearchViewModel): BindingFragment<FragmentSearchFriendBinding>() {
    override fun getLayoutResId() = R.layout.fragment_search_friend

    override val viewModel: SearchViewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }
    private val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private val viewModelQr: QrCodeViewModel by lazy {
        ViewModelProvider(this)[QrCodeViewModel::class.java]
    }
    override fun setupView() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = v
        var adapter =  FriendAdapter({ user ->
            InfoYourself.token?.let { token ->
                viewModelFriend.checkFriendOrFollower(token, user.userId, {
                    if (it == 1) {
                        DetailFriendDialog(
                            user,
                            this,
                            requireContext(),
                            {
                                viewModelFriend.unFriend(InfoYourself.token!!, user.userId)
                                refresh()
                            }).show()
                    } else if (it == 0) {
//                        DetailUserDialog(user, this, requireContext(), {
//                            InfoYourself.token?.let { token ->
//                                viewModelQr.setFollow(
//                                    token,
//                                    user.userId,
//                                    null,
//                                    { status ->
//                                        showToast("Đã gửi thành công")
//                                        refresh()
//                                    })
//                            }
//                        }, {
//                        }).show()
                        DialogBottomSheetUser(user, this, requireContext(), {
                            InfoYourself.token?.let { token ->
                                viewModelQr.setFollow(
                                    token,
                                    user.userId,
                                    null,
                                    { status ->
                                        showToast("Đã gửi thành công")
                                        refresh()
                                    })
                            }
                        }).show()
                    } else if (it == 2) {
                        DetailFollowerDialog(user, this, requireContext(), {
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
                            requireContext(),
                            {
                                viewModelFriend.cancelFollow(InfoYourself.token!!, user.userId)
                                refresh()
                            }).show()
                    }
                })
            }
        })
        binding.recyclerFriend.adapter = adapter
    }

    private fun refresh() {

    }
}
