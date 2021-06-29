package com.dinhtai.fchat.data.local

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.sql.Date
import java.sql.Timestamp

data class Message(
    @SerializedName("msg_id")
    val msgId:Int?,
    @SerializedName("content")
    var content: String,
    @SerializedName("type")
    val typeMessege: TypeMessege?,
    @SerializedName("create_date")
    val createDate: Timestamp?,
    @SerializedName("status")
    val status:SeenDefault?,
    @SerializedName("is_sender")
    val isSender:Boolean?,    // if me send, it is true
    @SerializedName("user")
    val user : User?,     //use in group chat
    @SerializedName("receiver_id")
    val receiverId: String?,
    @SerializedName("sender_id")
    val senderId: String?
):GeneraEntity, Serializable {
    override fun areItemsTheSame(newItem: GeneraEntity): Boolean {
        return newItem is Message && this.msgId == newItem.msgId
    }

    override fun areContentsTheSame(newItem: GeneraEntity): Boolean {
        return newItem is Message &&  this== newItem
    }
}
