package com.dinhtai.fchat.ui.group.create

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

class CreateGroupViewModel : RxViewModel() {
    private var _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private var _status = MutableLiveData<StatusRepository>()
    val status: LiveData<StatusRepository>  = _status
    fun setUsers(users: List<User>){
        _users.value = users
    }
    fun createGroup(token:String,nameGroup:String,members:String,avatarGroup:String?){
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).createGroup(token,nameGroup,members,avatarGroup)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                    Log.d("dddd",it.toString())
                    Log.d("create group ss",it.toString())
                }, {
                    Log.d("create group fail", it.toString())
                })
        )
    }
}
