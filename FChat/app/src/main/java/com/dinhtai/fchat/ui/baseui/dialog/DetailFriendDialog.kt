package com.dinhtai.fchat.ui.baseui.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BaseBottomSheetDialog
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.DialogInforFriendBinding
import com.dinhtai.fchat.ui.chat.ChatActivity
import com.dinhtai.fchat.utils.toast

class DetailFriendDialog(
    private val user: User,
    private val lifecyclerOner: LifecycleOwner,
    context: Context,
    private val listerCancelFollow: (() -> Unit)? = null
) : BaseBottomSheetDialog(context) {
    private lateinit var binding: DialogInforFriendBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DialogInforFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = lifecyclerOner
        binding.user = user
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonCancelFollow.setOnClickListener {
            listerCancelFollow?.let { it1 -> it1() }
            context.toast("Đã hủy kết bạn")
            dismiss()
        }
        binding.buttonChat.setOnClickListener {
            var intent = Intent(context,ChatActivity::class.java)
            intent.putExtra("user",user)
            context.startActivity(intent)
            dismiss()
        }
        setOnShowListener {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
