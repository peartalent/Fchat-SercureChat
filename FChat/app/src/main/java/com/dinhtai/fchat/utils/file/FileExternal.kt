package com.dinhtai.fchat.utils.file

import android.app.Application
import android.content.Context
import android.os.Environment
import com.dinhtai.fchat.data.local.KeyAES
import com.dinhtai.fchat.data.local.KeyRSA
import com.google.gson.Gson
import kotlinx.io.IOException
import java.io.*

class FileExternal(private val context: Context) {
    private val filepath = "fchat"

    val isExternalStorageReadOnly: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) true else false
        }
    val isExternalStorageAvailable: Boolean
        get() {
            val extStorageState = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED.equals(extStorageState)) true else false
        }

    fun writeFileRSA(data: String,fileName: String) {
        var myExternalFile = File(context.getExternalFilesDir(filepath), "rsa"+fileName+".txt")
        if (!myExternalFile.exists()) {
            myExternalFile.createNewFile();
        }
        try {
            val fw = FileWriter(myExternalFile)
            val bw = BufferedWriter(fw)
            bw.write(data)
            bw.close();
            fw.close();
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun writeFileAES(data: String, fileName: String) :Boolean {
        var myExternalFile = File(context.getExternalFilesDir(filepath),"aes"+ fileName+".txt")
        if (!myExternalFile.exists()) {
            myExternalFile.createNewFile();
        }
        try {
            val fw = FileWriter(myExternalFile)
            val bw = BufferedWriter(fw)
            bw.write(data)
            bw.close();
            fw.close();
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }
   // lưu theo userId
    fun readFileAES(fileName: String): KeyAES? {
        var myExternalFile = File(context.getExternalFilesDir(filepath), "aes"+fileName+".txt")
        if (!myExternalFile.exists()) {
            return null
        }
        try {
            var fileInputStream = FileInputStream(myExternalFile)
            var inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var text: String? = null
            var gson = Gson()
            while ({ text = bufferedReader.readLine(); text }() != null) {
                if (text!!.length > 1){
                   return gson.fromJson(text,KeyAES::class.java)
                }
            }
            fileInputStream.close()
        }  catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun readFileRSA(fileName: String): KeyRSA? {
        var myExternalFile = File(context.getExternalFilesDir(filepath), "rsa"+fileName+".txt")
        if (!myExternalFile.exists()) {
            return null
        }
        try {
            var fileInputStream = FileInputStream(myExternalFile)
            var inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var text: String? = null
            var gson = Gson()
            while ({ text = bufferedReader.readLine(); text }() != null) {
                if (text!!.length > 1){
                    return gson.fromJson(text,KeyRSA::class.java)
                }
            }
            fileInputStream.close()
        }  catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
