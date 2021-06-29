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
import com.dinhtai.fchat.databinding.ItemChatLeftGroupBinding
import com.dinhtai.fchat.databinding.ItemChatRightBinding
import com.dinhtai.fchat.utils.*
import com.dinhtai.fchat.utils.config.getUri
import com.dinhtai.fchat.utils.safe.AESCrypt
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.item_chat_left_group.view.*
import kotlinx.android.synthetic.main.item_chat_left_group.view.exoPlayerViewLeft
import kotlinx.android.synthetic.main.item_chat_left_group.view.linearImageLeft
import kotlinx.android.synthetic.main.item_chat_left_group.view.linearVoiceLeft
import kotlinx.android.synthetic.main.item_chat_left_group.view.textNoticeLeft
import kotlinx.android.synthetic.main.item_chat_left_group.view.textTime
import kotlinx.android.synthetic.main.item_chat_right.view.*
import kotlinx.android.synthetic.main.item_chat_right.view.imageContent
import kotlinx.android.synthetic.main.item_chat_right.view.linearContent
import kotlinx.android.synthetic.main.item_chat_right.view.textContent
import java.util.*


class ChatGroupAdapter(
    private val keyAES: KeyAES,
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
            val itemView = ItemChatLeftGroupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ChatLeftViewHolder(sentKeyListener, keyAES, itemView, listener)
        } else {
            val itemView = ItemChatRightBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ChatRightViewHolder(keyAES, itemView, listener, clickMenuPopUp)
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
        submitList(data)
    }

    inner class ChatLeftViewHolder(
        private val sentKeyListener: (Message) -> Unit,
        private val keyAES: KeyAES, private val itemLeft: ItemChatLeftGroupBinding,
        listener: (Message) -> Unit,
    ) : BaseViewHolder<Message, ViewBinding>(itemLeft, listener) {
        override fun onBind(itemData: Message) {
            super.onBind(itemData)
            itemLeft.message = itemData
            itemLeft.linearDeleteLeft.hide()
            Log.d("message item", itemData.toString())
            when (itemData.typeMessege) {
                TypeMessege.text -> {
                    var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
                    if (!content.isNullOrEmpty()) {
                        itemLeft.root.linearContent?.visibility = TextView.VISIBLE
                        itemLeft.root.imageContent?.visibility = ImageView.GONE
                        itemLeft.root.linearVoiceLeft?.visibility = View.GONE
                        itemLeft.root.textContent?.text = content
                    } else {
                        itemLeft.layoutChat.hide()
                    }
                }
                TypeMessege.img -> {
                    try {
                        itemLeft.root.linearContent?.visibility = TextView.GONE
                        itemLeft.root.imageContent?.visibility = ImageView.VISIBLE
                        itemLeft.root.linearVoiceLeft?.visibility = View.GONE
                        var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
                        Log.d("xxxxxxcontent",content.toString() + itemData.content)
                        itemLeft.root.imageContent?.let{ loadImageEncode(it, Base64.decode(content, Base64.DEFAULT)) }
                    } catch (e:Exception){
                        itemLeft.root.hide()
                        e.printStackTrace()
                    }
                }
                TypeMessege.audio -> {
                    itemLeft.root.linearContent?.visibility = TextView.GONE
                    itemLeft.root.linearImageLeft?.visibility = ImageView.GONE
                    itemLeft.root.linearVoiceLeft?.visibility = View.VISIBLE
                    itemLeft.root.exoPlayerViewLeft?.let {
                        var simpleExoPlayer = SimpleExoPlayer.Builder(itemLeft.root.context).build()
                        it.player = simpleExoPlayer
//                        messageAudios.add(it.player as SimpleExoPlayer)
                        // exo có thằng để quản lý các player, nên pause khi có thằng chạy
                        (it.player as SimpleExoPlayer).prepare(
                            itemLeft.root.context.mediaSource(
                                getUri(itemData.content)
                            )
                        )
                    }
                }
                TypeMessege.other -> {
                    itemLeft.root.textContent?.visibility = View.GONE
                    itemLeft.root.linearContent?.visibility = TextView.GONE
                    itemLeft.root.imageContent?.visibility = ImageView.GONE
                    itemLeft.root.linearVoiceLeft?.visibility = View.GONE
                    itemLeft.root.textTime?.visibility = View.GONE
                    itemLeft.root.imageAvatar?.visibility = View.GONE
                    itemLeft.root.textFullname?.visibility = View.GONE
                    itemLeft.root.textNoticeLeft?.let {
                        it.visibility = View.VISIBLE
                        var nowDate = Date()
                        var dbDate = Date(itemData.createDate!!.time)
                        if (itemData.content.contains("create")) {
                            itemLeft.textFullname.hide()
                            it.text =
                                "Bạn " + itemLeft.root.resources.getString(R.string.lable_create_group) + " vào lúc " + getDurationAsString(
                                    dbDate,
                                    nowDate
                                )
                        } else if (itemData.content.contains("add")) {
                            itemLeft.textFullname.hide()
                            var contentArr = itemData.content.split(":")
                            it.text =
                                itemData.user?.fullname + " " + itemLeft.root.resources.getString(R.string.lable_add_member_group) + " vào lúc " + getDurationAsString(
                                    dbDate,
                                    nowDate
                                )
                        } else if (itemData.content.equals("keyRequest")) {
                            itemLeft.root.textNoticeLeft.show()
                            itemLeft.root.textNoticeLeft.text = "Bạn của bạn yêu cầu key"
                            itemLeft.root.textNoticeLeft.setOnLongClickListener {
                                sentKeyListener(itemData)
                                true
                            }
                        }  else if (itemData.content.equals("delete")){
                            itemLeft.root.textDeleteLeft.show()
                            var content =itemLeft.root.resources.getString(R.string.lable_message_delete)
                            itemLeft.root.textDeleteLeft.text = content
                        }

                    }

                }
            }
        }
    }

    inner class ChatRightViewHolder(
        private val keyAES: KeyAES, private val itemBinding: ItemChatRightBinding,
        listener: (Message) -> Unit, private val clickMenuPopUp: (View, Message) -> Unit
    ) : BaseViewHolder<Message, ViewBinding>(itemBinding, listener) {
        override fun onBind(itemData: Message) {
            super.onBind(itemData)
            itemBinding.message = itemData
            Log.d("message chat right", itemData.toString())
            when (itemData.typeMessege) {
                TypeMessege.text -> {
                    var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
                    if (!content.isNullOrEmpty()) {
                        itemBinding.root.textContent?.text = content
                        itemBinding.layoutChat.setOnLongClickListener {
                            clickMenuPopUp(it, itemData)
                            true
                        }
                        itemBinding.root.linearContent?.visibility = TextView.VISIBLE
                        itemBinding.root.linearImageRight?.visibility = ImageView.GONE
                        itemBinding.root.linearVoice?.visibility = View.GONE
                        itemBinding.linearDelete.hide()
                    } else {
                        itemBinding.root.hide()
                    }
                }
                TypeMessege.img -> {
                    try {
                        var content = AESCrypt().getDecryptedString(keyAES.key, itemData.content)
                        itemBinding.root.imageContent?.let{ loadImageEncode(it, Base64.decode(content, Base64.DEFAULT)) }
                        itemBinding.root.linearContent?.visibility = TextView.GONE
                        itemBinding.root.linearImageRight?.visibility = ImageView.VISIBLE
                        itemBinding.root.linearVoice?.visibility = View.GONE
                        itemBinding.linearDelete.hide()
                        itemBinding.layoutChat.setOnLongClickListener {
                            clickMenuPopUp(it, itemData)
                            true
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                        itemBinding.root.hide()
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
                    itemBinding.layoutChat.setOnLongClickListener {
                        clickMenuPopUp(it, itemData)
                        true
                    }
                }
                TypeMessege.other -> {
                    itemBinding.root.linearContent?.visibility = TextView.GONE
                    itemBinding.root.linearImageRight?.visibility = ImageView.GONE
                    itemBinding.root.linearVoice?.visibility = View.GONE
                    itemBinding.linearDelete.hide()
                    itemBinding.root.textNotice?.let {
                        it.visibility = View.VISIBLE
                        var nowDate = Date()
                        var dbDate = Date(itemData.createDate!!.time)

                        if (itemData.content.contains("create")) {
                            it.text =
                                "Bạn " + itemBinding.root.resources.getString(R.string.lable_create_group) + " vào lúc " + getDurationAsString(
                                    dbDate,
                                    nowDate
                                )
                        } else if (itemData.content.contains("add")) {
                            var contentArr = itemData.content.split(":")
                            it.text =
                                "Bạn " + itemBinding.root.resources.getString(
                                    R.string.lable_add_member_group
                                ) + itemData.user?.fullname + " vào lúc " + getDurationAsString(
                                    dbDate,
                                    nowDate
                                )
                        } else if (itemData.content.equals("keyRequest")) {
                            it.hide()
                            itemBinding.layoutChat.hide()
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
}

