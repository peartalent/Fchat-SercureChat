package com.dinhtai.fchat.data.local

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.sql.Timestamp

data class Notification(
    @SerializedName("notification_id")val id: Int,
    val sender: User?,
    val group: Group?,
    val type:String,
    val content: String,
    @SerializedName("create_date") val createDate: Timestamp ,
    val status : Int =0   // 0 là chưa đọc, 1 là đọc rồi
): GeneraEntity, Serializable {

    override fun areItemsTheSame(newItem: GeneraEntity): Boolean {
        return newItem is Notification && this.id == newItem.id
    }

    override fun areContentsTheSame(newItem: GeneraEntity): Boolean {
        return newItem is User && this.equals(newItem)
    }

    override fun equals(other: Any?): Boolean = id == (other as Notification).id
}
