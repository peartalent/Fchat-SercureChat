package com.dinhtai.fchat.ui.baseui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.Notification
import com.dinhtai.fchat.databinding.ItemNotificationBinding

class NotificationAdapter (private val listener: (Notification)->Unit, private var listenerMore: ((Notification, View) -> Unit)):
    BaseAdapter<Notification, ItemNotificationBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<Notification, ItemNotificationBinding> {
        val itemView = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(itemView, listener,listenerMore)
    }

    class NotificationViewHolder(
        private val itemBinding: ItemNotificationBinding,
        listener: (Notification) -> Unit,
        private val listenerMore: ((Notification, View) -> Unit)
    )  : BaseViewHolder<Notification, ItemNotificationBinding>(itemBinding,listener){
        override fun onBind(itemData: Notification) {
            super.onBind(itemData)
            itemBinding.notification = itemData
            itemBinding.buttonMore.setOnClickListener { listenerMore(itemData,it) }
        }
    }
}
