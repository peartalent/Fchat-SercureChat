package com.dinhtai.fchat.utils

import android.telephony.PhoneNumberUtils
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

fun hiddenPartNumberPhone(numberPhone: String): String =
    if (checkNumberPhone(numberPhone)) numberPhone.replaceRange(0..6, "********") else ""

fun checkNumberPhone(numberPhone: String) = PhoneNumberUtils.isGlobalPhoneNumber(numberPhone)
fun createUUID() = UUID.randomUUID().toString().replace("-", "")
fun String.replaceToOne() = toUpperCase().replaceRange(1..(this.length - 1), "")
fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.sha1(): String {
    val md = MessageDigest.getInstance("SHA-1")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}
