package com.dinhtai.fchat.ui.baseui.adapter

import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BaseDiffUtil
import com.dinhtai.fchat.base.BaseViewHolder
import com.dinhtai.fchat.base.BindableAdapter
import com.dinhtai.fchat.data.local.KeyAES
import com.dinhtai.fchat.data.local.Message
import com.dinhtai.fchat.data.local.TypeMessege
import com.dinhtai.fchat.databinding.ItemChatLeftBinding
import com.dinhtai.fchat.databinding.ItemChatRightBinding
import com.dinhtai.fchat.utils.*
import com.dinhtai.fchat.utils.config.getUri
import com.dinhtai.fchat.utils.safe.AESCrypt
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.item_chat_left.view.*
import kotlinx.android.synthetic.main.item_chat_left.view.imageContent
import kotlinx.android.synthetic.main.item_chat_left.view.textContent
import kotlinx.android.synthetic.main.item_chat_right.view.*


class ChatAdapter(
    var keyAES: KeyAES,
    private val listener: (Message) -> Unit,
    private val clickMenuPopUp: (View, Message) -> Unit,
    private val sentKeyListener: (Message) -> Unit
) :
    ListAdapter<Message, BaseViewHolder<Message, ViewBinding>>(BaseDiffUtil<Message>()),
    BindableAdapter<List<Message>> {
    val LEFT = 1
    val RIGHT = 2
    private var messageAudios = mutableListOf<Player>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<Message, ViewBinding> {
        if (viewType == LEFT) {
            val itemView = ItemChatLeftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ChatLeftViewHolder(itemView, listener, sentKeyListener, keyAES)
        } else {
            val itemView = ItemChatRightBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ChatRightViewHolder(itemView, listener, clickMenuPopUp, keyAES)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Message, ViewBinding>, position: Int) {
        Log.d("message item khong thoi", getItem(position).toString())
        holder.onBind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).isSender ?: true)
            return RIGHT
        return LEFT
    }

    override fun setData(data: List<Message>?) {
//        messageAudios = data?.filter { it.typeMessege ==TypeMessege.audio }
        submitList(data)
    }

    private fun turnOfAllAudio() {
        messageAudios?.forEach {
            it.playWhenReady = false
        }
    }

    inner class ChatLeftViewHolder(
        private val itemLeft: ItemChatLeftBinding,
        listener: (Message) -> Unit,
        private val sentKeyListener: (Message) -> Unit,
        private val keyAES: KeyAES
    ) : BaseViewHolder<Message, ViewBinding>(itemLeft, listener) {
        override fun onBind(itemData: Message) {
            super.onBind(itemData)
            itemLeft.message = itemData
            itemLeft.root.linearDeleteLeft.hide()
            itemLeft.root.linearImageLeft?.hide()
            itemLeft.root.linearVoiceLeft?.hide()
            itemLeft.root.linearContentLeft.hide()
            itemLeft.root.textNoticeLeft.hide()
            Log.d("message item", itemData.toString())
            when (itemData.typeMessege) {
                TypeMessege.text -> {
                    var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
                    if (content.isNullOrBlank()) {
                        itemLeft.root.linearLeft?.hide()
                        itemLeft.root.hide()
                        itemLeft.root.textTime.hide()
                    } else {
                        itemLeft.root.textContent.show()
                        itemLeft.root.linearContentLeft.show()
                        itemLeft.root.textContent?.text = content
                    }
                }
                TypeMessege.img -> {
                    try {
                        itemLeft.root.linearImageLeft?.show()
                        Log.d("xxxxxcontent",itemData.content)
                        var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
                        itemLeft.root.imageContent?.let { loadImageEncode(it, Base64.decode(content, Base64.DEFAULT)) }
                    }   catch (e:Exception){
                        itemLeft.root.hide()
                        e.printStackTrace()
                    }
                }
                TypeMessege.audio -> {
                    itemLeft.root.linearVoiceLeft?.show()
                    itemLeft.root.exoPlayerViewLeft?.let {
                        var simpleExoPlayer = SimpleExoPlayer.Builder(itemLeft.root.context).build()
                        it.player = simpleExoPlayer
                        (it.player as SimpleExoPlayer).prepare(
                            itemLeft.root.context.mediaSource(
                                getUri(itemData.content)
                            )
                        )
                    }
                }
                TypeMessege.other -> {
                    if (itemData.content.equals("keyRequest")) {
                        itemLeft.root.textNoticeLeft.show()
                        itemLeft.root.textNoticeLeft.text = "Bạn của bạn yêu cầu key"
                        itemLeft.root.textNoticeLeft.setOnLongClickListener {
                            sentKeyListener(itemData)
                            true
                        }
                    } else if (itemData.content.equals("delete")) {
                        itemLeft.root.linearDeleteLeft.show()
                        var content =
                            itemLeft.root.resources.getString(R.string.lable_message_delete)
                        itemLeft.root.textDeleteLeft.text = content
                    }
                }
            }
        }
    }

    inner class ChatRightViewHolder(
        private val itemBinding: ItemChatRightBinding,
        listener: (Message) -> Unit,
        private val clickMenuPopUp: (View, Message) -> Unit,
        private val keyAES: KeyAES
    ) : BaseViewHolder<Message, ViewBinding>(itemBinding, listener) {
        override fun onBind(itemData: Message) {
            super.onBind(itemData)
            itemBinding.message = itemData
            when (itemData.typeMessege) {
                TypeMessege.text -> {
                    var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
                    if (content.isNullOrBlank()) {
                        itemBinding.root.linearContent?.hide()
                        itemBinding.root.textContent.hide()
                    } else {
                        itemBinding.root.textContent?.text = content
                        itemBinding.root.linearContent?.show()
                        itemBinding.linearContent.setOnLongClickListener {
                            clickMenuPopUp(it, itemData)
                            true
                        }
                    }
                    itemBinding.root.textNotice?.hide()
                    itemBinding.linearDelete.hide()
                    itemBinding.root.linearImageRight?.hide()
                    itemBinding.root.linearVoice?.hide()
                }
                TypeMessege.img -> {
                   // Log.d("xxxxxcontent",itemData.content)
                    try {
                        itemBinding.root.linearImageRight?.show()
                        var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
                        itemBinding.root.imageContent?.let { loadImageEncode(it, Base64.decode(content, Base64.DEFAULT)) }
                        itemBinding.root.linearContent?.visibility = TextView.GONE
                        itemBinding.root.linearVoice?.visibility = View.GONE
                        itemBinding.linearDelete.hide()
                        itemBinding.linearImageRight.setOnClickListener { listener(itemData) }
                        itemBinding.linearImageRight.setOnLongClickListener {
                            clickMenuPopUp(it, itemData)
                            true
                        }
                    }catch (e :Exception){
                        itemBinding.root.hide()
                        e.printStackTrace()
                    }
                }
                TypeMessege.audio -> {
                    itemBinding.root.linearContent?.visibility = TextView.GONE
                    itemBinding.root.linearImageRight?.visibility = ImageView.GONE
                    itemBinding.root.linearVoice?.visibility = View.VISIBLE
                    itemBinding.linearDelete.hide()
                    itemBinding.root.exoPlayerView?.let {
                        var simpleExoPlayer =
                            SimpleExoPlayer.Builder(itemBinding.root.context).build()
                        it.player = simpleExoPlayer
                        messageAudios.add(it.player as SimpleExoPlayer)
                        (it.player as SimpleExoPlayer).prepare(
                            itemBinding.root.context.mediaSource(
                                getUri(itemData.content)
                            )
                        )
                    }
                    itemBinding.linearVoice.setOnLongClickListener {
                        clickMenuPopUp(it, itemData)
                        true
                    }
                }
                TypeMessege.other -> {
                    itemBinding.root.linearContent?.hide()
                    itemBinding.root.linearImageRight?.hide()
                    itemBinding.root.linearVoice?.hide()
                    itemBinding.linearDelete.hide()
                    if (itemData.content.equals("keyRequest")) {
                        itemBinding.root.hide()
                    } else if (itemData.content.equals("delete")) {
                        itemBinding.linearDelete.show()
                        var content =
                            itemBinding.root.resources.getString(R.string.lable_message_delete)
                        itemBinding.textDelete.text = content
                    }
                }
            }
        }
    }
}

