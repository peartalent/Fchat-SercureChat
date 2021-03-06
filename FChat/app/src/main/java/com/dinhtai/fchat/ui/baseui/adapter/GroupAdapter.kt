package com.dinhtai.fchat.ui.baseui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.Group
import com.dinhtai.fchat.data.local.TypeMessege
import com.dinhtai.fchat.databinding.ItemGroupBinding
import com.dinhtai.fchat.utils.RandomColor
import com.dinhtai.fchat.utils.file.FileExternal
import com.dinhtai.fchat.utils.loadImage
import com.dinhtai.fchat.utils.replaceToOne
import com.dinhtai.fchat.utils.safe.AESCrypt
import kotlinx.android.synthetic.main.item_group.view.*

class GroupAdapter(private val listener: (Group) -> Unit) : BaseAdapter<Group, ItemGroupBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<Group, ItemGroupBinding> {
        val itemView = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(itemView, listener)
    }

    class GroupViewHolder(
        private val itemBinding: ItemGroupBinding, listener: (Group) -> Unit ,
    ) : BaseViewHolder<Group, ItemGroupBinding>(itemBinding, listener) {
        override fun onBind(itemData: Group) {
            super.onBind(itemData)
            itemBinding.group = itemData
            if (itemData.messages !=null && itemData.messages.size>=0){
                when(itemData.messages!![0].typeMessege){
                    TypeMessege.text -> {
                        val fileExternal= FileExternal(itemBinding.root.context)
                        fileExternal.readFileAES("group"+itemData.id)?.let {
                            AESCrypt().getDecryptedString(it.key,itemData.messages!![0].content)?.let {
                                itemData.messages!![0].content= it
                            }
                        }

                    }
                    TypeMessege.img ->{
                        if (itemData.messages!![0].isSender == true){
                            itemData.messages!![0].content = "B???n ???? g???i m???t h??nh ???nh"
                        } else{
                            itemData.messages!![0].content = "???? g???i m???t h??nh ???nh"
                        }

                    }
                    TypeMessege.audio ->{
                        if (itemData.messages!![0].isSender == true){
                            itemData.messages!![0].content = "B???n ???? g???i m???t tin nh???n tho???i"
                        } else{
                            itemData.messages!![0].content = "???? g???i m???t tin nh???n tho???i"
                        }
                    }
                    TypeMessege.video ->{
                        if (itemData.messages!![0].isSender == true){
                            itemData.messages!![0].content = "B???n ???? g???i m???t video"
                        } else{
                            itemData.messages!![0].content = "???? g???i ???????c m???t video"
                        }
                    }
                }
                if (itemData.avatar != null && itemData.avatar.contains("image")){
                    itemBinding.imageAvatar?.visibility = View.VISIBLE
                    itemBinding.textAvatar?.visibility = View.GONE
                    itemBinding.root.imageAvatar?.let { loadImage(it,itemData.avatar) }
                } else{
                    itemBinding.imageAvatar?.visibility = View.GONE
                    itemBinding.textAvatar?.visibility = View.VISIBLE
                    itemData.name?.let {
                        itemBinding.textAvatar?.background.setTint(RandomColor().getColor(itemData.name?.replaceToOne()))
                        itemBinding.textAvatar?.text= itemData.name?.replaceToOne()
                    }

                }
            }
        }
    }
}
