package com.dinhtai.fchat.ui.baseui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.AdminDefault
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ItemFriendBinding

class MemberGroupAdapter(
    private val listener: (User) -> Unit,
    private val listenerDelete: ((View,User) -> Unit)?=null,
) : BaseAdapter<User, ItemFriendBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<User, ItemFriendBinding> {
        val itemView = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberGroupViewHolder(itemView, listener,listenerDelete)
    }

    class MemberGroupViewHolder(
        private val itemFriendBinding: ItemFriendBinding,
        listener: (User) -> Unit,
        private val listenerDelete: ((View,User) -> Unit)?=null
    ) : BaseViewHolder<User, ItemFriendBinding>(itemFriendBinding, listener) {
        override fun onBind(itemData: User) {
            super.onBind(itemData)
            itemFriendBinding.friend = itemData
            itemFriendBinding.textRole?.visibility = View.VISIBLE
            itemFriendBinding.textRole?.text = if (itemData.role == AdminDefault.admin) "QTV" else "Thành Viên"
            itemFriendBinding.root.setOnLongClickListener {
                listenerDelete?.let { it1 -> it1(it,itemData) }
                false
            }
        }
    }
}
