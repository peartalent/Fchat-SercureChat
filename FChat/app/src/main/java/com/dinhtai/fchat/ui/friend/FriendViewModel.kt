package com.dinhtai.fchat.ui.friend

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FriendViewModel : RxViewModel() {
    private val _friends = MutableLiveData<List<User>>()
    private val _friendOns = MutableLiveData<List<User>>()
    private val _isFriend = MutableLiveData<Boolean>()
    private val _follow = MutableLiveData<List<User>>()
    val friendOns: LiveData<List<User>>
        get() = _friendOns
    val friends: LiveData<List<User>>
        get() = _friends
    val isFriend: LiveData<Boolean>
        get() = _isFriend
    val follow: LiveData<List<User>>
        get() = _follow
    private val _friendOrFollower = MutableLiveData<StatusRepository>()
    val friendOrFollower: LiveData<StatusRepository>
        get() = _friendOrFollower
    fun getFriends(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getFriends(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _friends.value = it.sortedByDescending { it.status==OnOff.on }
                    _friendOns.value = it.filter { user -> user.status == OnOff.on}
                    Log.d("friends", it.toString())
                }, {
                    Log.d("friend model", it.toString())
                })
        )
    }

    fun searchFriends(name:String?) : List<User>? {
        if (name.isNullOrEmpty()){
           return  _friends.value
        }
        var rs = _friends.value?.let {it.filter { it.fullname!!.toUpperCase()!!.contains(name.toUpperCase())  }}
        Log.d("rssssssssssss",rs.toString())
        return rs
    }

    fun checkFriendOrFollower(token: String, id2: String,listener: ((Int) -> Unit?)? =null) {
        _friendOrFollower.value = StatusRepository(4,4,null,null,null)
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).checkFriendOrFollower(token,id2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({sp ->
                           _friendOrFollower.value = sp
                    Log.d("status nè",sp.toString())
                    listener?.let { listener(sp.status) }
                }, {
                    Log.d("friend model", it.toString())
                })
        )
    }

    fun unFriend(token: String, id2: String,listener: ((StatusRepository) -> Unit?)?=null) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).unFriend(token,id2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({listener?.let { listener->listener(it) }},{it.printStackTrace()})
        )
    }

    fun checkFriend(token: String, userId: String, listener: ((Int) -> Unit?)? =null) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getFriends(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _friends.value = it
                    var isf = 0
                    it?.let {
                        it.forEach {
                            if (it.userId.equals(userId)){
                                _isFriend.value = true
                                isf = 1
                            }
                        }
                        _follow.value?.forEach {
                            if (it.userId.equals(userId)){
                                isf = 2
                            }
                        }
                        listener?.let { it1 -> it1(isf) }
                    }

                    Log.d("friends", it.toString())
                }, {
                    Log.d("friend model", it.toString())
                })
        )
    }

    fun acceptFollow(token: String, id2: String,listener: (() -> Unit?)? =null) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).acceptFollow(token, id2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it?.let { listener?.let { it1 -> it1() } }
                    Log.d("friend accepted", it.toString())
                }, {
                    Log.d("follow model", it.toString())
                })
        )
    }

    fun refuseFollow(token: String, id2: String,listener: (() -> Unit?)? =null) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).refuseFollow(token, id2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it?.let { listener?.let { it1 -> it1() } }
                    Log.d("follow refuse", it.toString())
                }, {
                    Log.d("follow model", it.toString())
                })
        )
    }

    fun cancelFollow(token: String, id2: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).cancelFollow(token, id2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("follow cancel", it.toString())
                }, {
                    Log.d("follow model", it.toString())
                })
        )
    }

    fun getFollows(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getFollows(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _follow.value = it
                    Log.d("follows model", it.toString())
                }, {
                    Log.d("follow model", it.toString())
                })
        )
    }

    init {
        if (InfoYourself.token != null) {
            getFriends(InfoYourself.token!!)
            getFollows(InfoYourself.token!!)
        }

    }
}
