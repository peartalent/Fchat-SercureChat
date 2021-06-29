package com.dinhtai.fchat.ui.profile.qrcode

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.dinhtai.fchat.GetOTP
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.databinding.ActivityQrCodeBinding
import com.dinhtai.fchat.utils.addFragment
import com.dinhtai.fchat.utils.generateQrCode
import com.dinhtai.fchat.utils.replaceFragment
import com.dinhtai.fchat.utils.toast
import kotlinx.android.synthetic.main.activity_get_otp.*

class QrCodeActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var binding:ActivityQrCodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupPermissions()
        addFragment(ScanQrCodeFragment(),R.id.frameQrcode)
        binding.materialToolbar.setNavigationOnClickListener {
            var intent = Intent(applicationContext, GetOTP::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.buttonShowQrCode.setOnClickListener {
            replaceFragment(ShowQrCodeFragment(),R.id.frameQrcode,false)
        }
        binding.buttonScanQrCode.setOnClickListener {
            replaceFragment(ScanQrCodeFragment(),R.id.frameQrcode,false)
        }
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode ==1){
            if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                toast("Bạn cần mở cemara")
            }
            else{
                replaceFragment(ScanQrCodeFragment(),R.id.frameQrcode)
            }
        }
    }

}
