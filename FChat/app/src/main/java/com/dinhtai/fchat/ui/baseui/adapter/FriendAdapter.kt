package com.dinhtai.fchat.ui.baseui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ItemFriendBinding
import com.dinhtai.fchat.utils.hide
import com.dinhtai.fchat.utils.show

class FriendAdapter(
    private val listener: (User) -> Unit,
    private val listenerMore: ((User, View) -> Unit)? = null,
    private var modeMore: Boolean = false
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
        return FriendViewHolder(itemView, listener, listenerMore, modeMore)
    }

    class FriendViewHolder(
        private val itemFriendBinding: ItemFriendBinding,
        listener: (User) -> Unit,
        private val listenerMore: ((User, View) -> Unit)? = null,
        private var modeMore: Boolean = false
    ) : BaseViewHolder<User, ItemFriendBinding>(itemFriendBinding, listener) {
        override fun onBind(itemData: User) {
            super.onBind(itemData)
            Log.d("friends adapter", itemData.toString())
            itemFriendBinding.friend = itemData
            if (!modeMore) {
                itemFriendBinding.buttonMore.hide()
            } else {
                itemFriendBinding.buttonMore.show()
            }

            itemFriendBinding.buttonMore.setOnClickListener { v ->
                listenerMore?.let { it(itemData, v) }
            }
        }
    }
}
