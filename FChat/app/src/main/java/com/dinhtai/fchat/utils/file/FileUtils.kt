package com.dinhtai.fchat.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.dinhtai.fchat.data.local.Contact
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


const val WAVE_HEADER_SIZE = 44

val Context.recordFile: File
    get() = File(filesDir, "rec.wav")

fun Context.prepareFilePart(part: Uri, partName: String): MultipartBody.Part {
    var file = part.toFile()
    //var type =  contentResolver.getType(Uri.fromFile(file))
    var x =MediaType.parse("image/*")
    var requestBody =
        RequestBody.create(x, file)
    return MultipartBody.Part.createFormData(partName,file.name,requestBody)
}

fun Activity.getPath(uri: Uri?): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = managedQuery(uri, projection, null, null, null)
    startManagingCursor(cursor)
    val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor.moveToFirst()
    return cursor.getString(column_index)
}

fun Context.getPath(uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor =
        getContentResolver().query(uri, projection, null, null, null) ?: return null
    val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor.moveToFirst()
    val s: String = cursor.getString(column_index)
    cursor.close()
    return s
}
fun File.toMediaSource(): MediaSource =
    DataSpec(this.toUri())
        .let { FileDataSource().apply { open(it) } }
        .let { DataSource.Factory { it } }
        .let { ProgressiveMediaSource.Factory(it, DefaultExtractorsFactory()) }
        .createMediaSource(MediaItem.fromUri(this.toUri()))

fun Context.getContacts(): List<Contact> {
    val contactList: MutableList<Contact> = ArrayList()
    val contacts = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )
    if (contacts != null) {
        while (contacts.moveToNext()) {
            val name =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    .replace(" ", "").sha256()
            val obj = Contact(name, number)
            contactList.add(obj)
        }
    }
    contacts?.close()
    return contactList
}
