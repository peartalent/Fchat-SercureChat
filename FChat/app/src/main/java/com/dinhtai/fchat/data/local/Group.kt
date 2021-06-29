package com.dinhtai.fchat.data.local

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.sql.Timestamp

data class Group(
    @SerializedName("group_id")
    val id: Int,
    val name: String,
    @SerializedName("create_date")
    val createDate: Timestamp?,
    val avatar: String?,
    val role: AdminDefault? = AdminDefault.default,
    val messages: List<Message>?,
    val members: List<User>?
) : GeneraEntity, Serializable {
    override fun areItemsTheSame(newItem: GeneraEntity): Boolean {
        return newItem is Group && this.id == newItem.id
    }

    override fun areContentsTheSame(newItem: GeneraEntity): Boolean {
        return newItem is Group && this.equals(newItem)
    }

    override fun equals(other: Any?): Boolean {
        return this.id == (other as Group).id
    }
}

