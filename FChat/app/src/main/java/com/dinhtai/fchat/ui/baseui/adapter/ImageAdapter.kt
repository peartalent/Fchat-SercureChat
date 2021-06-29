package com.dinhtai.fchat.ui.baseui.adapter

import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BaseAdapter
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.data.local.KeyAES
import com.dinhtai.fchat.data.local.Message
import com.dinhtai.fchat.databinding.ItemImageViewBinding
import com.dinhtai.fchat.utils.hide
import com.dinhtai.fchat.utils.loadImageEncode
import com.dinhtai.fchat.utils.safe.AESCrypt
import com.dinhtai.fchat.utils.toast
import kotlinx.android.synthetic.main.item_chat_left.view.*

class ImageAdapter(private val listener: (Message) -> Unit, private val keyAES: KeyAES) :
    BaseAdapter<Message, ItemImageViewBinding>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<Message, ItemImageViewBinding> {
        val itemView = ItemImageViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(itemView, listener, keyAES)
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<Message, ItemImageViewBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        Log.d("item image", position.toString())
        holder.onBind(getItem(position))

    }

    class ImageViewHolder(
        private val itemBinding: ItemImageViewBinding,
        listener: (Message) -> Unit,
        private val keyAES: KeyAES
    ) : BaseViewHolder<Message, ItemImageViewBinding>(itemBinding, listener) {
        override fun onBind(itemData: Message) {
            super.onBind(itemData)
            var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
            if (content == null) {
                itemBinding.imageView.setImageResource(R.drawable.ic_cancel)
                itemBinding.root.context.toast("Lỗi ảnh đã bị mã hóa")
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
