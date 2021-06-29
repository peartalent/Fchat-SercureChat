package com.dinhtai.fchat.ui.image

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.Message
import com.dinhtai.fchat.data.local.TypeMessege

class ImageViewModel : RxViewModel() {
    private var _message = MutableLiveData<Message>()
    private var _urlImage = MutableLiveData<String>()
    private var _messageImages = MutableLiveData<List<Message>>()
    val message: LiveData<Message> get() = _message
    val urlImage: LiveData<String> get() = _urlImage
    val messageImages: LiveData<List<Message>> get() = _messageImages
    fun getMessage(message: Message){
        _message.value = message
        _urlImage.value = message.content
        Log.d("url image",message.content)
    }
    fun getMessageImages(messages: List<Message>){
        _messageImages.value = messages.filter { it->it.typeMessege == TypeMessege.img }
        Log.d("image list",_messageImages.value.toString())
    }
}
