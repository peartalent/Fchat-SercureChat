package com.dinhtai.fchat.utils.safe

import android.util.Base64
import kotlinx.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AESCrypt {
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private var encry: ByteArray? = null

    var generateKey = (1..32)    //vi aes chỉ lấy 14 16 32 byte
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("");

    private fun secretKey(key: String): SecretKeySpec {
        return SecretKeySpec(key.toByteArray(), "AES")
    }
     /*
     mã hóa
      */
    fun getEncryptedString(key: String, data: String): String? {
         try {
             val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
             cipher.init(Cipher.ENCRYPT_MODE, secretKey(key))
             return Base64.encodeToString(cipher.doFinal(data.toByteArray()), Base64.DEFAULT)
         } catch (e : Exception){
             e.printStackTrace()
             return null
         }
    }
    /*
    giải mã
     */
    fun getDecryptedString(key: String, data: String?): String? {
        if (data == null) return null
        try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey(key))
            return String(cipher.doFinal(Base64.decode(data, Base64.DEFAULT)))
        }catch (e : Exception){
            e.printStackTrace()
             return null
        }
    }
}
