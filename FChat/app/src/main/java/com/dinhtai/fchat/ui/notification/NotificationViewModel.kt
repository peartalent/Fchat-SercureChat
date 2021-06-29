package com.dinhtai.fchat.ui.notification

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NotificationViewModel :RxViewModel(){
    private var _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications
    private var _status = MutableLiveData<StatusRepository>()
    val status: LiveData<StatusRepository> get() = _status

    fun getNotificationById(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getNotification(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                   _notifications.value=it.sortedByDescending { it.createDate }
                    Log.d("notification all",it.toString())
                }, {
                    Log.d("notification bug", it.toString())
                })
        )
    }
    fun updateStatusNotification(token: String,id:Int) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).updateNotification(token,id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value =it
                    Log.d("notification update status",it.toString())
                }, {
                    Log.d("notification bug", it.toString())
                })
        )
    }
    fun updateAllStatusNotification(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).updateAllNotification(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value =it
                    Log.d("notification update status",it.toString())
                }, {
                    Log.d("notification bug", it.toString())
                })
        )
    }
    fun deleteNotification(token: String,id:Int) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).deleteNotification(token,id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value =it
                    Log.d("notification delete",it.toString())
                }, {
                    Log.d("notification bug", it.toString())
                })
        )
    }

    fun deleteAllNotification(token: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).deleteAllNotification(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value =it
                    Log.d("notification delete",it.toString())
                }, {
                    Log.d("notification bug", it.toString())
                })
        )
    }
    init {
        InfoYourself.token?.let { getNotificationById(it) }
    }
}
