package com.dinhtai.fchat.ui.profile.qrcode

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.QrCode
import com.dinhtai.fchat.utils.generateQrCode
import kotlinx.android.synthetic.main.fragment_qr_code_show.*

class ShowQrCodeFragment :Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_qr_code_show, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        InfoYourself.userID?.let {
            imageQrCode.setImageBitmap(activity?.generateQrCode(QrCode(it).toJson()))
        }
    }

}
