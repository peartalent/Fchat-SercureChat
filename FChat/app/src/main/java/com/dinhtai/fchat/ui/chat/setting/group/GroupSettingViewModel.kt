package com.dinhtai.fchat.ui.chat.setting.group

import android.app.Dialog
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GroupSettingViewModel : RxViewModel() {
    private var _messageImages = MutableLiveData<List<Message>>()
    private var _group = MutableLiveData<Group>()
    private var _memberSelects = MutableLiveData<List<User>?>()
    val messageImages: LiveData<List<Message>> get() = _messageImages
    private var _me = MutableLiveData<User>()
    val me: LiveData<User> get() = _me
    private var _isAdmin = MutableLiveData<Boolean>(false)
    val isAdmin: LiveData<Boolean> = _isAdmin
    val group: LiveData<Group> get() = _group
    val memberSelects: LiveData<List<User>?> = _memberSelects
    private var _status = MutableLiveData<StatusRepository>()
    val status: LiveData<StatusRepository>  = _status

    fun getInfoGroupById(token: String, id: Int) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).getMessageGroupByGroupId(token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _group.value = it
                    it?.let {
                        it.members?.forEach {
                            if (it.userId == InfoYourself.userID) _me.value = it
                        }
                    }
                    _memberSelects.value = it.members
                    InfoYourself.userID?.let { it1 -> isAdmin(it1,it.members) }
                    it.messages?.let {
                        _messageImages.value = it.sortedBy { it.createDate?.time }
                        _messageImages.value = it.filter { it -> it.typeMessege == TypeMessege.img }
                    }
                }, {
                    Log.d("setting group", "lấy thông tin thất bại")
                })
        )
    }

    fun updateGroupById(token: String, name: String, id: Int, avatar: String?,dialog: Dialog?=null) {
        dialog?.dismiss()
        Log.d("update group",name+avatar)
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).updateGroup(token, name, id, avatar)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                }, {
                    Log.d("setting group", "update thất bại")
                })
        )
    }

    fun isAdmin(userId: String,members :List<User>?) {
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

    fun addMemberToGroup(token: String, newMember: String, groupId: Int,listener:(()->Unit)?=null) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).addMemberToGroup(token, groupId, newMember)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                    listener?.let { listener() }
                }, {
                    Log.d("setting group", "update thất bại")
                })
        )
    }

    fun deleteMemberToGroup(token: String,newMember: String, groupId: Int) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).deleteMemberToGroup(token, groupId, newMember)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _status.value = it
                }, {
                    Log.d("setting group", "update thất bại")
                })
        )
    }
}
