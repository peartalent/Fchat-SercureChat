package com.dinhtai.fchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.KeyRSA
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import com.dinhtai.fchat.ui.main.MainActivity
import com.dinhtai.fchat.utils.file.FileExternal
import com.dinhtai.fchat.utils.hiddenPartNumberPhone
import com.dinhtai.fchat.utils.isEditTextNull
import com.dinhtai.fchat.utils.safe.RSACrypt
import com.dinhtai.fchat.utils.sha256
import com.dinhtai.fchat.utils.toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_get_otp.*
import kotlinx.android.synthetic.main.activity_sent_otp.*
import kotlinx.android.synthetic.main.activity_sent_otp.buttonConfirm
import kotlinx.android.synthetic.main.activity_sent_otp.progressBar
import retrofit2.Retrofit
import java.util.*
import java.util.concurrent.TimeUnit


class SentOTP : AppCompatActivity() {
    private val TAG = "SentOTP"
    private var numberphone: String? = ""
    private lateinit var retrofit: Retrofit
    lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sent_otp)
        supportActionBar?.hide()
        setupOTPInputs()
        Log.d("log send", "đã vào send otp")
        retrofit = ApiBuilder.retrofit
        imageBack.setOnClickListener {
            var intent = Intent(applicationContext, GetOTP::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        auth = FirebaseAuth.getInstance()
        storedVerificationId = intent.getStringExtra(GetOTP.STORED_VEROFICATION_ID)!!
        numberphone = intent.getStringExtra(GetOTP.NUMBER_PHONE)
        storedVerificationId?.let { Log.d(TAG, it) }
        textPhone.text =
            resources.getString(R.string.we_send_to_otp_phone) + numberphone?.let {
                hiddenPartNumberPhone(it)
            }
        buttonConfirm.setOnClickListener {
            if (isEditTextNull(editOTP1, editOTP2, editOTP3, editOTP4, editOTP5, editOTP6)) {
                toast(resources.getString(R.string.lable_enter_all))
                return@setOnClickListener
            }
            var otp =
                editOTP1.text.toString() + editOTP2.text.toString() + editOTP3.text.toString() + editOTP4.text.toString() + editOTP5.text.toString() + editOTP6.text.toString()
            visiableProgressBar(View.VISIBLE)
            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                storedVerificationId.toString(), otp
            )
            signInWithPhoneAuthCredential(credential)
        }
        val timer = object: CountDownTimer(59000, 1000) {
            var timeEnd =60;
            override fun onTick(millisUntilFinished: Long) {
                timeEnd--
                textTimeEndCode.text  = timeEnd.toString()
            }
            override fun onFinish() {
                finish()
            }
        }
        timer.start()
        textResentOTP.setOnClickListener {
              callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    visiableProgressBar(View.GONE)
//                signInWithPhoneAuthCredential(credential)
                    Log.d(TAG,"onVerificationCompleted LOGIN HOAN THANH")
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    visiableProgressBar(View.GONE)
                    toast(e.message.toString())
                    Log.d(TAG,"onVerificationFailed:"+"e.message.toString()")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG,"onCodeSent:$verificationId")
                    storedVerificationId = verificationId
                    timer.cancel()
                    timer.start()
                }
            }
            Log.d("phone number ","+84"+numberphone)
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+84"+numberphone) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    visiableProgressBar(View.GONE)
                    var intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    Create use
                    var fileExternal = FileExternal(applicationContext);
                    var key = RSACrypt().generateKeyPair()
                    var publicKey = ""
                    var privateKey =""
                    if (fileExternal.readFileRSA(numberphone!!) == null) {
                        privateKey = android.util.Base64.encodeToString(
                            key.private.encoded,
                            android.util.Base64.DEFAULT
                        )
                        publicKey = android.util.Base64.encodeToString(
                            key.public.encoded,
                            android.util.Base64.DEFAULT
                        )
                        fileExternal.writeFileRSA(
                            Gson().toJson(
                                KeyRSA(
                                    privateKey,
                                    publicKey,
                                    Date().toString()
                                )
                            ), numberphone!!
                        )
                    } else{
                        publicKey = FileExternal(applicationContext).readFileRSA(numberphone!!)!!.publicKey
                    }
                    var service = retrofit.create(Api::class.java)
                    service.createUser(numberphone!!.sha256(), publicKey)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            { result ->
                                Log.d("log result send", result.toString())
                                val sharedPref = this@SentOTP?.getSharedPreferences(
                                    getString(R.string.app_name),
                                    Context.MODE_PRIVATE
                                )
                                with(sharedPref.edit()) {
                                    putString(getString(R.string.token), result.token)
                                    putString(getString(R.string.user_id), result.user_id)
                                    putString("phone", numberphone)
                                    commit()
                                }

                                startActivity(intent)
                                finish()
                            },
                            { error ->
                                error.message?.let {
                                    toast(it)
                                    Log.d("log err", it)
                                }
                                var intent =
                                    Intent(applicationContext, GetOTP::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        )
                } else {
                    toast(resources.getString(R.string.lable_failed_otp))
                }
            }

    }
    //Logic GONE and VISIBLE
    private fun visiableProgressBar(mode: Int) {
        if (mode == View.GONE) {
            progressBar.visibility = View.GONE
            buttonConfirm.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.VISIBLE
            buttonConfirm.visibility = View.INVISIBLE
        }
    }

    private fun setupOTPInputs() {
        editOTP1.doOnTextChanged { text, start, before, count ->
            if (!text.toString().trim().isEmpty()) editOTP2.requestFocus()
        }
        editOTP2.doOnTextChanged { text, start, before, count ->
            if (!text.toString().trim().isEmpty()) editOTP3.requestFocus()
        }
        editOTP3.doOnTextChanged { text, start, before, count ->
            if (!text.toString().trim().isEmpty()) editOTP4.requestFocus()
        }
        editOTP4.doOnTextChanged { text, start, before, count ->
            if (!text.toString().trim().isEmpty()) editOTP5.requestFocus()
        }
        editOTP5.doOnTextChanged { text, start, before, count ->
            if (!text.toString().trim().isEmpty()) editOTP6.requestFocus()
        }
    }
}
