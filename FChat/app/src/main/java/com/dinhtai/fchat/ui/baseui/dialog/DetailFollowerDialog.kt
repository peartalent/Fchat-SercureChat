package com.dinhtai.fchat.ui.baseui.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BaseBottomSheetDialog
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.DialogInforFollowerBinding
import com.dinhtai.fchat.utils.toast

class DetailFollowerDialog(
    private val user: User,
    private val lifecyclerOner: LifecycleOwner,
    context: Context,
    private val listenerAcceptFollow: (() -> Unit),
    private val listenerRefuse: (() -> Unit)
) : BaseBottomSheetDialog(context) {
    private lateinit var binding: DialogInforFollowerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DialogInforFollowerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = lifecyclerOner
        binding.user = user
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonAcceptFollow.setOnClickListener {
            listenerAcceptFollow()
            context.toast("Hai người giờ đã là bạn")
            dismiss()
        }
        binding.buttonRefuse.setOnClickListener {
            listenerRefuse()
            context.toast("Đã từ chối")
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
