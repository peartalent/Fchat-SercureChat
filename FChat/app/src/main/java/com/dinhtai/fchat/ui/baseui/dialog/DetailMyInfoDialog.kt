package com.dinhtai.fchat.ui.baseui.dialog

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LifecycleOwner
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BaseBottomSheetDialog
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.databinding.DialogMyInfomationBinding
import com.dinhtai.fchat.ui.main.ActivityMainViewModel
import com.dinhtai.fchat.utils.*

class DetailMyInfoDialog(
    private val lifecyclerOner: LifecycleOwner,
    context: Activity,
    private val viewModel: ActivityMainViewModel,
    private val callback:()->Unit
) : BaseBottomSheetDialog(context) {
    private lateinit var binding: DialogMyInfomationBinding
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = DialogMyInfomationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = lifecyclerOner
        token = InfoYourself.token!!
        viewModel.getMyUser(token)
        binding.vm = viewModel
        window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        viewModel.myUser.observe(lifecyclerOner, {
            if (it.sex.equals("nam")) binding.radioMale.isChecked = true
            else if (it.sex.equals("nữ")) binding.radioFemale.isChecked = true
        })
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.imageAvatar.setOnClickListener {
             callback()
        }
        binding.textName.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrBlank()) {
                binding.imageReload.show()
                binding.imageDone.show()
            } else {
                binding.imageReload.hide()
                binding.imageDone.hide()
            }
        }
        binding.imageDone?.setOnClickListener {
            if (!binding.textName.text.isNullOrBlank()){
                 viewModel.updateNameUser(token,binding.textName.text.toString())
            }
        }

        binding.imageReload.setOnClickListener {
            binding.textName.setText("")
        }

        binding.radioGroup.setOnCheckedChangeListener { group, i ->
            when (i) {
                R.id.radio_male -> {
                    Log.d("da chon", "nam + " + i + "  " + R.id.radio_male)
                    viewModel.updateSexUser(token, "nam")
                }

                R.id.radio_female -> {
                    Log.d("da chon", "nub + " + i)
                    viewModel.updateSexUser(token, "nữ")
                }
            }
        }
        binding.textName.setOnFocusChangeListener { view, b ->
            if (!b && !(binding.textName.text).isNullOrEmpty()) viewModel.updateNameUser(
                token,
                binding.textName.text.toString().trim()
            )
        }
        setOnDismissListener { viewModel.getMyUser(token) }
        setOnShowListener {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

}
