package com.dinhtai.fchat.ui.profile.qrcode

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.StatusRepository
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class QrCodeViewModel : RxViewModel() {
    private var _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user
    private var _status = MutableLiveData<StatusRepository>()
    val status: LiveData<StatusRepository> get() = _status
    fun setFollow(token:String,id2:String,preface:String?, listener: (StatusRepository)->Unit){
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).createFollow(token, id2, preface)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    listener(it)
                }, {
                    Log.v("err",it.message.toString())
                })
        )
    }
    fun getUserById(id:String){
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getClient(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                   _user.value =it
                }, {
                    Log.v("err",it.message.toString())
                })
        )
    }
}
