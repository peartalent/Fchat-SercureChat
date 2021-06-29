package com.dinhtai.fchat.data.local

import com.google.gson.Gson

data class QrCode(val content: String, val header: String = "Fchat-no1") {
    companion object {
        const val HEADER = "Fchat-no1"
    }

    fun toJson(): String = Gson().toJson(this)
}
