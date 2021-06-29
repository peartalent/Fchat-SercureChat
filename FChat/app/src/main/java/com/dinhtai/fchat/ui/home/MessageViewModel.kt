package com.dinhtai.fchat.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class MessageViewModel : RxViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    private val _chatFrients = MutableLiveData<List<Message>>()
    private var _myUser = MutableLiveData<User>()
    val chatFrients: LiveData<List<Message>>
        get() = _chatFrients
    val message: LiveData<List<Message>>
        get() = _messages
    val myUser: LiveData<User> get() = _myUser

    fun getMyUser(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getUser(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("message all", it.toString())
                    _myUser.value = it
                    Log.d("my user", it.toString())
                }, {
                    Log.d("user model", it.toString())
                })
        )
    }

     fun loadShortMessage(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getShortMessages(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _messages.value = it
                    Log.d("all message short", it.toString())
                }, {
                    Log.d("item message short", it.toString())
                })
        )
    }

    init {
        InfoYourself.token?.let {
            loadShortMessage(it)
            getMyUser(it)
        }
    }
}
