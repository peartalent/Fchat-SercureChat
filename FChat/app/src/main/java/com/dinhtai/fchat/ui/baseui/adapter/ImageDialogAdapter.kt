package com.dinhtai.fchat.ui.baseui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.KeyAES
import com.dinhtai.fchat.data.local.Message
import com.dinhtai.fchat.databinding.ItemImageBinding
import com.dinhtai.fchat.utils.loadImageEncode
import com.dinhtai.fchat.utils.safe.AESCrypt
import com.dinhtai.fchat.utils.show
import com.dinhtai.fchat.utils.toast

class ImageDialogAdapter (private val listener: (Message) -> Unit, private val keyAES: KeyAES) :
    BaseAdapter<Message, ItemImageBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<Message, ItemImageBinding> {
        val itemView = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(itemView, listener,keyAES)
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<Message, ItemImageBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        holder.onBind(getItem(position))

    }

    class ImageViewHolder(
        private val itemBinding: ItemImageBinding,
        listener: (Message) -> Unit,
        private val keyAES: KeyAES
    ) : BaseViewHolder<Message, ItemImageBinding>(itemBinding, listener) {
        override fun onBind(itemData: Message) {
            super.onBind(itemData)
            var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
            if (content == null) {
                itemBinding.imageView.setImageResource(R.drawable.ic_baseline_cancel_24)
                itemBinding.textNotice.show()
            }
            else
                itemBinding.imageView?.let {
                    loadImageEncode(
                        it,
                        Base64.decode(content, Base64.DEFAULT)
                    )
                }
        }
    }
}
