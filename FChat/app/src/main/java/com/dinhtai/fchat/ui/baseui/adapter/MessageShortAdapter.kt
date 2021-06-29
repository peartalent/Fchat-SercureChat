package com.dinhtai.fchat.ui.baseui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.Message
import com.dinhtai.fchat.data.local.TypeMessege
import com.dinhtai.fchat.databinding.ItemMessageBinding
import com.dinhtai.fchat.utils.file.FileExternal
import com.dinhtai.fchat.utils.safe.AESCrypt

class MessageShortAdapter(private val listener: (Message) -> Unit) :
    BaseAdapter<Message, ItemMessageBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<Message, ItemMessageBinding> {
        val itemView = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageShortViewHolder(itemView, listener)
    }

    class MessageShortViewHolder(
        private val itemMessageBinding: ItemMessageBinding,
        listener: (Message) -> Unit
    ) : BaseViewHolder<Message,ItemMessageBinding>(itemMessageBinding,listener){
        override fun onBind(itemData: Message) {
            super.onBind(itemData)
            Log.d("item message short that",itemData.toString())
            when (itemData.typeMessege){
                TypeMessege.text -> {
                    val fileExternal= FileExternal(itemMessageBinding.root.context)
                     fileExternal.readFileAES(itemData.user!!.userId)?.let {
                         AESCrypt().getDecryptedString(it.key,itemData.content)?.let {
                             itemData.content = it
                         }
                     }
                }
                TypeMessege.img ->{
                    if (itemData.isSender == true){
                        itemData.content = "Bạn đã gửi một hình ảnh"
                    } else{
                        itemData.content = "Bạn đã nhận được một hình ảnh"
                    }

                }
                TypeMessege.audio ->{
                    if (itemData.isSender == true){
                        itemData.content = "Bạn đã gửi một tin nhắn thoại"
                    } else{
                        itemData.content = "Bạn đã nhận được một tin nhắn thoại"
                    }
                }
                TypeMessege.video ->{
                    if (itemData.isSender == true){
                        itemData.content = "Bạn đã gửi một video"
                    } else{
                        itemData.content = "Bạn đã nhận được một video"
                    }
                }
            }
            itemMessageBinding.shortMsg = itemData

        }
    }
}
