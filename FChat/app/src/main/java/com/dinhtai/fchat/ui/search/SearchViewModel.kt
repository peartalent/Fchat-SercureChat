package com.dinhtai.fchat.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.Group
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SearchViewModel : RxViewModel() {
    private val _friends = MutableLiveData<List<User>>()
    private var _groups = MutableLiveData<List<Group>>()
    private var _groupSearch = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> get() = _groups
    val groupSearch  : LiveData<List<Group>> get() = _groupSearch
    val friends: LiveData<List<User>> get() = _friends

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

    fun searchGroups(name:String?) {
        if (name.isNullOrEmpty()){
            _groupSearch.value =  _groups.value
            Log.d("group ne",_groupSearch.value.toString())
        } else{
            _groupSearch.value= _groups.value?.let {it.filter { it.name.toUpperCase()!!.contains(name.toUpperCase()) }}
            Log.d("group ne",_groupSearch.value.toString())

        }
    }

    fun searchFriends(token: String, name: String?) {
        if (name.isNullOrBlank()){
            _friends.value = listOf()
        } else{
            disposables.add(
                ApiBuilder.retrofit.create(Api::class.java).searchFriend(token, name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _friends.value = it
                    }, {

                    })
            )
        }

    }
    init {
        InfoYourself.token?.let {
            Log.d("grouppppppp",it)
            getAllGroup(it)
        }
    }
}
