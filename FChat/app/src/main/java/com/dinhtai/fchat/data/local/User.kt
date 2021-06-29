package com.dinhtai.fchat.data.local

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.sql.Timestamp

data class User constructor(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("public_key")
    val publicKey: String?,
    val fullname: String?,
    @SerializedName(value = "avatar", alternate = ["avartar"])
    val avatar: String?,
    @SerializedName("last_online")
    val lastOnline: Timestamp?,
    var status: OnOff? = OnOff.off,
    @SerializedName("token_client")
    val tokenClient: String = "",
    var role: AdminDefault? = AdminDefault.default,
    var sex :String?=null,
    val check:Int? =0 // 0 bt, 1 friend// 2 follow// 3 đã gửi lời mời
    ) : GeneraEntity, Serializable {

    override fun areItemsTheSame(newItem: GeneraEntity): Boolean {
        return newItem is User && this.userId == newItem.userId
    }

    override fun areContentsTheSame(newItem: GeneraEntity): Boolean {
        return newItem is User && this.equals(newItem)
    }

    override fun equals(other: Any?): Boolean = userId == (other as User).userId

}
