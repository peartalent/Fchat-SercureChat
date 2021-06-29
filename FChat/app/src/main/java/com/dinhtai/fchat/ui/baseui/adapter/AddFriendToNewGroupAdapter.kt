package com.dinhtai.fchat.ui.baseui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ItemAddFriendToGroupBinding

class AddFriendToNewGroupAdapter(private val listener: (User) -> Unit) :
    BaseAdapter<User, ItemAddFriendToGroupBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<User, ItemAddFriendToGroupBinding> {
        val itemView = ItemAddFriendToGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AddFriendToNewGroupViewHolder(itemView, listener)
    }

    class AddFriendToNewGroupViewHolder(
        private val itemBinding: ItemAddFriendToGroupBinding,
        private val listener: (User) -> Unit
    ) : BaseViewHolder<User, ItemAddFriendToGroupBinding>(itemBinding, listener) {
        override fun onBind(itemData: User) {
            super.onBind(itemData)
            itemBinding.user = itemData
            itemBinding.linearLayout.setOnClickListener {
                listener(itemData)
            }
            itemBinding.buttonRemove.setOnClickListener {
                listener(itemData)
            }
        }
    }
}
