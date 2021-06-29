package com.dinhtai.fchat.ui.group

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GroupViewModel : RxViewModel() {
    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>>
        get() = _groups
    private val _status = MutableLiveData<StatusRepository>()
    val status: LiveData<StatusRepository>
        get() = _status
    fun getAllGroup(token:String){
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getAllGroup(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _groups.value =it.sortedByDescending{it.messages?.get(0)?.createDate?.time}
                    Log.d("groups",it.toString())
                }, {
                    Log.d("groups model", it.toString())
                })
        )
    }

    fun deleteGroup(token:String,groupId:Int){
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).deleteGroup(token,groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                }, {
                })
        )
    }

    fun searchGroups(name:String?) : List<Group>? {
        if (name.isNullOrEmpty()){
            return  _groups.value
        }
        var rs = _groups.value?.let {it.filter { it.name.toUpperCase()!!.contains(name.toUpperCase()) }}
        Log.d("rssssssssssss",rs.toString())
        return rs
    }
    init {
        InfoYourself.token?.let {
            Log.d("grouppppppp",it)
            getAllGroup(it)
        }
    }
}
