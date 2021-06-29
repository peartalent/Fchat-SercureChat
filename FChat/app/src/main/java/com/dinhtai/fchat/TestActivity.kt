package com.dinhtai.fchat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.io.ByteArrayOutputStream


class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        var url ="http://192.168.100.138:3000/image/6efd690020264448bcc8978317daa0a11620715561314.png"
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    resource.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    var imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
                    val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                    imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    image.setImageBitmap(decodedImage)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }
}
