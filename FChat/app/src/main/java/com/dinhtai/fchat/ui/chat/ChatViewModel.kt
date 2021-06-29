package com.dinhtai.fchat.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class ChatViewModel() : RxViewModel() {
    private var _messages = MutableLiveData<List<Message>>()
    private var _countMessagess = MutableLiveData<Int>()
    var _messageImages = MutableLiveData<List<Message>>()
    private var _group = MutableLiveData<Group>()
    private var _memberSelects = MutableLiveData<List<User>?>()
    private var _status = MutableLiveData<StatusRepository>()
    val status: LiveData<StatusRepository> = _status
    private var _isAdmin = MutableLiveData<Boolean>(false)
    val isAdmin: LiveData<Boolean> = _isAdmin
    val messageImages: LiveData<List<Message>> get() = _messageImages
    val group: LiveData<Group> get() = _group
    val memberSelects: LiveData<List<User>?> = _memberSelects
    fun countMessages(): LiveData<Int> {
        return _countMessagess
    }

    val messages: LiveData<List<Message>>
        get() = _messages

    fun getMessagesById(token: String, id: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getMessagesById(token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("message all", it.toString())
                    _messages.value = it
                    _countMessagess.value = it.size
                    _messageImages.value = it.filter { it -> it.typeMessege == TypeMessege.img }
                    Log.d("adapter message", _countMessagess.value.toString())
                }, {
                    Log.d("messsage model", it.toString())
                })
        )
    }

    fun getMessageGroupById(token: String, id: Int) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getMessageGroupByGroupId(token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("messageg groupp all", it.toString())
                    _group.value = it
                    it.members?.let {
                        _memberSelects.value = it
                    }
                    InfoYourself.userID?.let { it1 -> isAdmin(it1, it.members) }
                    Log.d("xxxxxxxxxxxxxxxxxxxxx", it.members.toString())
                    it.messages?.let {
                        _messages.value = it.sortedBy { it.createDate?.time }
                        _countMessagess.value = it.size
                        _messageImages.value = it.filter { it -> it.typeMessege == TypeMessege.img }
                    }
                }, {
                    Log.d("messsage model bug group", it.toString())
                })
        )
    }

    fun isAdmin(userId: String, members: List<User>?) {
        members?.let {
            it.forEach {
                if (it.userId.equals(userId) && it.role != null && it.role == AdminDefault.admin) {
                    _isAdmin.value = true
                    Log.d("admin ne", InfoYourself.userID ?: "null rồi")
                    return@let
                }
            }
        }
    }

    fun createNotification(token: String, type: String, content: String, userId: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java)
                .createNotification(token, type, content, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                    Log.d("xxxxxxxxxxxxxxxxxxxxx", it.toString())
                }, {
                    Log.d("failse", it.toString())
                })
        )
    }

    fun sendMessage(message: Message) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).sendUser(
                message.senderId!!,
                message.receiverId!!,
                message.content,
                message.createDate!!,
                message.typeMessege!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {
                    Log.d("Messsage model", it.toString())
                })

        )
    }
    fun updateStatusMessages(token: String,userId: String) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).updateStatusMessage(token,userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }
}
