package com.dinhtai.fchat.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.Location
import com.dinhtai.fchat.data.local.StatusRepository
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody

class ActivityMainViewModel : RxViewModel() {
    private var _myUser = MutableLiveData<User>()
    val myUser: LiveData<User> get() = _myUser
    var _status = MutableLiveData<StatusRepository>()
    val status: LiveData<StatusRepository> get() = _status

    private var _statusLogin = MutableLiveData<StatusRepository>()
    val statusLogin: LiveData<StatusRepository> get() = _statusLogin
    fun getMyUser(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getUser(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("my user", it.toString())
                    _myUser.value = it
                    InfoYourself.fullname = it.fullname ?: "NO NAME"
                    InfoYourself.avatar = it.avatar
                    Log.d("my user", _myUser.toString())
                }, {
                    Log.d("user model", it.toString())
                })
        )
    }

    fun logout(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).logout(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, {
                })
        )
    }

    fun login(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).login(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("login ne",it.toString())
                    _statusLogin.value = it
                }, {
                })
        )
    }

    fun setLocation(token: String, location: Location) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java)
                .setLocation(token, location.latitude, location.longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun setContact(token: String, contacts: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).setContact(token, contacts)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun updateNameUser(token: String, name: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).updateNameUser(token, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                    getMyUser(token)
                }, {})
        )
    }

    fun updateSexUser(token: String, sex: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).updateSexUser(token, sex)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                    getMyUser(token)
                }, {})
        )
    }

    fun updateAvatarUser(token: String, avatar: MultipartBody.Part) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).updateAvatarUser(token, avatar)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                    getMyUser(token)
                }, {})
        )
    }

    init {
        InfoYourself.token?.let {
            getMyUser(it)
        }
    }
}
