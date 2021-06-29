package com.dinhtai.fchat.utils.safe
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.Exception


class RSACrypt{
    private val KEY_SIZE =1024
    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        generator.initialize(KEY_SIZE, SecureRandom())
        val keypair = generator.genKeyPair()
        return keypair
    }

    fun covertKeyPair(publicK: String,privateK: String):KeyPair{
        val keyFactory = KeyFactory.getInstance("RSA")
        val pubKey = keyFactory.generatePublic(X509EncodedKeySpec( Base64.decode(publicK,Base64.DEFAULT)))
        val priKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec( Base64.decode(privateK,Base64.DEFAULT)))
        return KeyPair(pubKey,priKey)
    }
    fun covertPublicKey(publicK: String):PublicKey?{
        try {
            val keyFactory = KeyFactory.getInstance("RSA")
            val pubKey = keyFactory.generatePublic(X509EncodedKeySpec( Base64.decode(publicK,Base64.DEFAULT)))
            return pubKey
        }
        catch (e : Exception){
            e.printStackTrace()
            return null;
        }
    }
    // mã óa
    fun getEncryptedString(publicKey: PublicKey?, data: String): String? {
        if (publicKey == null) return null
        try {
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return Base64.encodeToString(cipher.doFinal(data.toByteArray()), Base64.DEFAULT)
        }   catch (e:Exception){
            e.printStackTrace()
            return null
        }

    }
    // giải mã
    fun getDecryptedString(privateKey: PrivateKey?, data: String?): String? {
        if (privateKey == null || data == null) return null
        try {
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            return String(cipher.doFinal(Base64.decode(data, Base64.DEFAULT)))
        }   catch (e:Exception){
            e.printStackTrace()
           return null
        }

    }

}
