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
import com.dinhtai.fchat.databinding.DialogInfoUnfollowBinding
import com.dinhtai.fchat.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialog

class DetailFollowedDialog(
    private val user: User,
    private val lifecyclerOner: LifecycleOwner,
    context: Context,
    private val listerCancelFollow: (() -> Unit)? = null
) : BaseBottomSheetDialog(context) {
    private lateinit var binding: DialogInfoUnfollowBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogInfoUnfollowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = lifecyclerOner
        binding.user = user
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonCancelFollow.setOnClickListener {
            listerCancelFollow?.let { it1 -> it1() }
            context.toast("Đã hủy")
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
