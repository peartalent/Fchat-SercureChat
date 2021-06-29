package com.dinhtai.fchat.ui.profile.qrcode

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.QrCode
import com.dinhtai.fchat.data.local.StatusRepository
import com.dinhtai.fchat.ui.baseui.dialog.DetailFollowerDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailFriendDialog
import com.dinhtai.fchat.ui.baseui.dialog.DetailUserDialog
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.utils.isFchatQrCode
import com.dinhtai.fchat.utils.toast
import com.google.gson.Gson


class ScanQrCodeFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    val viewModel: QrCodeViewModel by lazy {
        ViewModelProvider(this)[QrCodeViewModel::class.java]
    }
    val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_scaner, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scannerView = view.findViewById<CodeScannerView>(R.id.scannerView)
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                if (activity.isFchatQrCode(it.text)) {
                    var qrCode = Gson().fromJson(it.text, QrCode::class.java)
                    viewModel.getUserById(qrCode.content)
                    viewModel.user.observe(viewLifecycleOwner,{ user->
                        InfoYourself.token?.let { token ->
                            viewModelFriend.checkFriend(token, user.userId, {
                                if (it == 1) {
                                    DetailFriendDialog(
                                        user,
                                        this,
                                        requireContext(),
                                        {
                                            viewModelFriend.getFollows(InfoYourself.token!!)
                                            viewModelFriend.getFriends(InfoYourself.token!!)
                                        }).show()
                                } else if (it == 0) {
                                    DetailUserDialog(user, this,  requireContext(), {
                                        InfoYourself.token?.let { token ->
                                            viewModel.setFollow(
                                                token,
                                                user.userId,
                                                null,{})
                                        }
                                    }, {

                                    }).show()
                                } else if (it == 2) {
                                    DetailFollowerDialog(user, this,  requireContext(), {
                                        InfoYourself.token?.let { token ->
                                            viewModelFriend.acceptFollow(
                                                token,
                                                user.userId
                                            )
                                        }
                                    }, {
                                        InfoYourself.token?.let { token ->
                                            viewModelFriend.refuseFollow(
                                                token,
                                                user.userId
                                            )
                                        }
                                    }).show()
                                }
                            })
                        }
                    })
                } else {
                    activity.toast(it.text)
                    Log.d("xxxxxx", it.text)
                }
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun showToast(status: StatusRepository){
        activity?.runOnUiThread {
            if (status.status ==1){
                activity?.toast("Thành công")
            } else {
                activity?.toast("Thất bại, có thể 2 người đã là bạn hoặc bạn đã gửi lời mời trước đó")
            }
        }
    }
}
