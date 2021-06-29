package com.dinhtai.fchat.ui.baseui.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BaseBottomSheetDialog
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.DialogInfoUserBinding
import com.dinhtai.fchat.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialog

class DialogBottomSheetUser(
    private val user: User,
    private val lifecyclerOner: LifecycleOwner,
    context: Context,
    private val listerFollow: (() -> Unit)
) : BaseBottomSheetDialog(context) {
    private lateinit var binding: DialogInfoUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogInfoUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = lifecyclerOner
        binding.user = user
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonFollow.setOnClickListener {
            listerFollow()
            context.toast("Đã gửi lời mời kết bạn")
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
