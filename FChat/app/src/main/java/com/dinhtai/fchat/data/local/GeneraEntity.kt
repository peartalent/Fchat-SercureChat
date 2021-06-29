package com.dinhtai.fchat.data.local

interface GeneraEntity {
    fun areItemsTheSame(newItem: GeneraEntity): Boolean
    fun areContentsTheSame(newItem: GeneraEntity): Boolean
}
