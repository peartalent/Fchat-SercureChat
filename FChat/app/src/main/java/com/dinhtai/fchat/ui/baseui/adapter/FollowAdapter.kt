package com.dinhtai.fchat.ui.baseui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ItemInvitationBinding
import kotlinx.android.synthetic.main.item_invitation.view.*

class FollowAdapter (private val listener: (User)->Unit, private val Yeslistener: (User) -> Unit,
                    private val NoLisener:(User) ->Unit): BaseAdapter<User, ItemInvitationBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<User, ItemInvitationBinding> {
        val itemView = ItemInvitationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FollowViewHolder(itemView, listener,Yeslistener,NoLisener)
    }

    class FollowViewHolder(
        private val itemInvitationBinding: ItemInvitationBinding,
        listener: (User) -> Unit,
        private val yesListener: (User) -> Unit,
        private val noLisener:(User) ->Unit
    ) : BaseViewHolder<User, ItemInvitationBinding>(itemInvitationBinding, listener) {
        override fun onBind(itemData: User) {
            super.onBind(itemData)
            itemInvitationBinding.follow = itemData
            itemInvitationBinding.root.imageYes.setOnClickListener {
                Log.d("follow acc",itemData.toString())
                yesListener(itemData)
            }
            itemInvitationBinding.root.imageNo.setOnClickListener {
                Log.d("follow acc",itemData.toString())
                noLisener(itemData)
            }
        }
    }
}
