package com.dinhtai.fchat.ui.baseui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.KeyAES
import com.dinhtai.fchat.data.local.Message
import com.dinhtai.fchat.databinding.DialogShowImageBinding
import com.dinhtai.fchat.ui.baseui.adapter.ImageDialogAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class DialogBottomSheetShowImage(var messages: List<Message>,
    private val keyAES: KeyAES
    ,private val activity: Activity,private val listener:(Message)->Unit) :
    BottomSheetDialogFragment() {
    private lateinit var binding: DialogShowImageBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DataBindingUtil
            .inflate<DialogShowImageBinding>(inflater, R.layout.dialog_show_image, container, false)
            .apply { binding = this }.root
        return view
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var adapter = ImageDialogAdapter({
            listener(it)
        },keyAES)
        dialog?.let {
            it.setOnShowListener {
                var bottomSheet =
                    (it as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                var behavior = BottomSheetBehavior.from(bottomSheet!!)
                var layoutParams = bottomSheet.layoutParams
                layoutParams?.let {
                    val displayMetrics = DisplayMetrics()
                    (activity).windowManager.defaultDisplay.getMetrics(
                        displayMetrics
                    )
                    layoutParams.height = displayMetrics.heightPixels
                }
                bottomSheet.layoutParams = layoutParams
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
        adapter.setData(messages)
        binding.recyclerImages.adapter = adapter
        binding.buttonCancel.setOnClickListener { dismiss() }
    }
}
