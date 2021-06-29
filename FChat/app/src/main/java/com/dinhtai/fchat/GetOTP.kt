package com.dinhtai.fchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dinhtai.fchat.ui.main.MainActivity
import com.dinhtai.fchat.utils.checkNumberPhone
import com.dinhtai.fchat.utils.isEditTextNull
import com.dinhtai.fchat.utils.toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_get_otp.*
import java.util.concurrent.TimeUnit

class GetOTP : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val TAG = "LOGIN_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_otp)
        supportActionBar?.hide()
        auth=FirebaseAuth.getInstance()

        var currentUser = auth.currentUser
        if(currentUser != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        buttonContinue.setOnClickListener{
            if (isEditTextNull(editNumberPhone)){
                toast(resources.getString(R.string.lable_enter_numberphone))
                return@setOnClickListener
            }
            if (!checkNumberPhone(editNumberPhone.text.toString())){
                toast(resources.getString(R.string.lable_check_numberphone))
                return@setOnClickListener
            }
            visiableProgressBar(View.VISIBLE)
            login()
        }
        // Callback function for Phone Auth
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
                resendToken = token
                Log.d(TAG,token.toString())
                visiableProgressBar(View.GONE)
                var intent = Intent(applicationContext,SentOTP::class.java)
                intent.putExtra(STORED_VEROFICATION_ID,storedVerificationId)
                intent.putExtra(NUMBER_PHONE,editNumberPhone.text?.trim().toString())
                startActivity(intent)
            }
        }

    }

    private fun login() {
        var number=editNumberPhone.text.toString().trim()
        number="+84"+number

        sendVerificationcode(number)
    }

    private fun sendVerificationcode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success::: "+ user)
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
    private fun visiableProgressBar( mode:Int){
        if (mode == View.GONE) {
            progressBar.visibility= View.GONE
            buttonContinue.visibility=View.VISIBLE
        }   else{
            progressBar.visibility= View.VISIBLE
            buttonContinue.visibility=View.INVISIBLE
        }
    }
    companion object{
        val STORED_VEROFICATION_ID = "storedVerificationId"
        val NUMBER_PHONE = "numberphone"
    }
}
