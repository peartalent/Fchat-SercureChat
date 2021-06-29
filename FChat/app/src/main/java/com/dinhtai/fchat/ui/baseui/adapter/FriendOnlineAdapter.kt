package com.dinhtai.fchat.ui.baseui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ItemFriendOnlineBinding

class FriendOnlineAdapter (private val listener: (User)->Unit):
    BaseAdapter<User, ItemFriendOnlineBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<User, ItemFriendOnlineBinding> {
        val itemView = ItemFriendOnlineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FriendViewHolder(itemView, listener)
    }

    class FriendViewHolder(
        private val itemFriendBinding: ItemFriendOnlineBinding,
        listener: (User) -> Unit,
    )  : BaseViewHolder<User, ItemFriendOnlineBinding>(itemFriendBinding,listener){
        override fun onBind(itemData: User) {
            super.onBind(itemData)
            Log.d("friends adapter",itemData.toString())
            itemFriendBinding.friend = itemData
        }
    }
}

