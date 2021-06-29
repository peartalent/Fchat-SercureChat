package com.dinhtai.fchat.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BindableAdapter
import com.dinhtai.fchat.data.local.*
import com.dinhtai.fchat.utils.config.ConfigAPI.URL
import com.dinhtai.fchat.utils.config.mapType
import com.dinhtai.fchat.utils.safe.AESCrypt
import com.mpt.android.stv.Slice
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.io.ByteArrayOutputStream
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
@BindingAdapter("data")
fun <T> setRecyclerViewProperties(recyclerView: RecyclerView, data: List<T>?) {
    (recyclerView.adapter as? BindableAdapter<List<T>>)?.setData(data)
}

@BindingAdapter("image")
fun loadImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrBlank()) {
        Glide.with(view.context)
            .load(URL + imageUrl)
            .placeholder(R.drawable.ic_photo)
            .error(R.drawable.ic_baseline_cancel_24)
            .into(view)
    }
}
@BindingAdapter("encodeImage")
fun loadImageEncode(view: ImageView, byte: ByteArray?) {
    if (byte != null && byte.size >0) {
        Glide.with(view.context)
            .asDrawable()
            .load(byte)
            .placeholder(R.drawable.ic_photo)
            .error(R.drawable.ic_baseline_cancel_24)
            .into(view)
    }
}

@BindingAdapter("image_group")
fun loadImageGroup(view: de.hdodenhof.circleimageview.CircleImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrBlank()) {
        view.visibility = View.VISIBLE
        Glide.with(view.context)
            .load(URL + imageUrl)
            .placeholder(R.drawable.ic_photo)
            .error(R.drawable.ic_baseline_cancel_24)
            .into(view)
    }
}

@BindingAdapter("title_group")
fun loadTitleGroup(view: TextView, title: String?) {
    if (!title.isNullOrBlank()) {
        view.text = title
    }
}

@BindingAdapter("uri")
fun loadImageUri(view: ImageView, imageUri: Uri?) {
    if (imageUri != null) {
        view.setImageURI(imageUri)
    }
}

@BindingAdapter("image")
fun loadImage(view: de.hdodenhof.circleimageview.CircleImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrBlank()) {
        //     view.visibility = View.VISIBLE
        Glide.with(view.context)
            .load(URL + imageUrl)
            .placeholder(R.drawable.ic_photo)
            .error(R.drawable.ic_baseline_cancel_24)
            .into(view)
    }
}
//@BindingAdapter("imageUri")
//fun loadByteImage(view: de.hdodenhof.circleimageview.CircleImageView, uri: Uri?) {
//    if (!uri.isNullOrBlank()) {
//        //     view.visibility = View.VISIBLE
//        Glide.with(view.context)
//            .load(uri)
//            .placeholder(R.drawable.ic_photo)
//            .error(R.drawable.ic_baseline_cancel_24)
//            .into(view)
//    }
//}

@BindingAdapter("content")
fun loadContent(view: TextView, message: Message?) {
    if (message != null) {
        if (message.status == SeenDefault.default && message.isSender ==false) {
            view.text = message.content
            view.setTextColor(Color.BLACK)
            view.typeface = Typeface.DEFAULT_BOLD
        } else {
            view.text = message.content
        }
    }
}

@BindingAdapter("avatar")
fun loadAvatarText(view: TextView, g: Group?) {
    if (g != null && g.avatar == null) {
        view?.visibility = View.VISIBLE
        view?.text = g.name?.replaceToOne()
        view?.background.setTint(RandomColor().getColor(g.name?.replaceToOne()))
    } else view.visibility = View.GONE
}

@BindingAdapter("uri")
fun loadAudio(view: com.dinhtai.fchat.data.local.media.player.SimpleExoPlayerView, url: String?) {
    if (!url.isNullOrEmpty()) {
        view.prepare(Uri.parse(URL + url))
    }
}

@BindingAdapter("status")
fun loadOnOff(view: ImageView, status: String?) {
    if (status == OnOff.on.toString()) {
        view.setImageResource(R.drawable.ic_online)
    } else {
        view.setImageResource(R.drawable.ic_off)
    }
}

@BindingAdapter("date")
fun loadDate(view: TextView, date: Timestamp?) {
    if (date != null) {
        var nowDate = Date()
        var dbDate = Date(date.time)
        view.text = getDurationAsString(dbDate, nowDate)
    }
}

@BindingAdapter("hide")
fun hideBackgroundChat(view: ImageView, messages: List<Message>?) {
    if (messages != null) {
        if (messages.size < 5) {
            var countImg = 0
            messages.forEach {
                if (it.typeMessege == TypeMessege.img) countImg++
            }
            if (countImg > 4) {
                view.visibility = View.GONE
            }
        } else {
            view.visibility = View.GONE
        }

    }
}

@BindingAdapter("content_group")
fun loadContentGroup(view: TextView, message: Message?) {
    if (message != null) {
        var content = message.user?.fullname + ": " + message.content
        message.isSender?.let {
            if (it) {
                content = message.content
            }
        }

        if (message.typeMessege == TypeMessege.other) {
            if (message.content.equals("create")) {
                if (message.isSender != null && message.isSender) {
                    content =
                        "Bạn" + " " + view.resources.getString(R.string.lable_create_group)
                } else
                    content =
                        message.user?.fullname + " " + view.resources.getString(R.string.lable_create_group)
            }
        }
        view.text = content

    }
}


@BindingAdapter("textSize")
fun loadSizeCollection(view: TextView, list: List<Any>?) {
    if (list != null) {
        view.text = list.size.toString()
    }
}

@BindingAdapter("textFollow")
fun loadSizeFollow(view: TextView, list: List<Any>?) {
    if (list != null && list.size>0) {
        view.show()
        view.text = view.resources.getText(R.string.title_number_of_friend_invitations).toString() + " " + list.size
    } else view.hide()
}

@BindingAdapter("textFriendOnline")
fun loadFriendOnline(view: TextView, list: List<User>?) {

    if (list != null && list?.size!! >0) {
        var uOnline = list?.filter { user -> user.status==OnOff.on  }
        if (uOnline.size>0){
            view.text = view.resources.getText(R.string.title_online).toString() + " " + uOnline?.size
        }

    }
}

@BindingAdapter("status")
fun loadStatusNotification(view: androidx.constraintlayout.widget.ConstraintLayout, status: Int?) {
    status?.let {
        if (it == 1) view.setBackgroundColor(Color.WHITE)
        else view.setBackgroundColor(view.rootView.resources.getColor(R.color.silver10))
    }
}

@BindingAdapter("content")
fun loadContentNotification(
    stvMarksDown: com.mpt.android.stv.SpannableTextView,
    notification: Notification?
) {
    notification?.let {
        when (notification.type) {
            "2", "4", "5", "8", "9" -> {
                stvMarksDown.reset()
                stvMarksDown.addSlice(
                    Slice.Builder(notification.sender?.fullname)
                        .style(Typeface.BOLD)
                        .build()
                )
                stvMarksDown.addSlice(
                    Slice.Builder(mapType[notification.type])
                        .build()
                )
                stvMarksDown.display()
            }
            "1", "3", "7" -> {
                stvMarksDown.reset()
                stvMarksDown.addSlice(
                    Slice.Builder(notification.sender?.fullname)
                        .style(Typeface.BOLD)
                        .build()
                )
                stvMarksDown.addSlice(
                    Slice.Builder(mapType[notification.type])
                        .build()
                )
                stvMarksDown.addSlice(
                    Slice.Builder(notification.group?.name)
                        .style(Typeface.BOLD)
                        .build()
                )
                stvMarksDown?.let { it.display() }
            }
            "6" -> {
                stvMarksDown.reset()
                stvMarksDown.addSlice(
                    Slice.Builder(mapType[notification.type])
                        .build()
                )
                stvMarksDown.addSlice(
                    Slice.Builder(notification.sender?.fullname)
                        .style(Typeface.BOLD)
                        .build()
                )
                stvMarksDown.display()
            }
        }

    }
}

@BindingAdapter("dateMessage")
fun loadDateToListMessages(view: TextView, messages: List<Message>?) {
    if (messages != null && messages.get(0).createDate != null) {
        var nowDate = Date()
        var dbDate = Date(messages.get(0).createDate!!.time)
        view.text = getDurationAsString(dbDate, nowDate)
    }
}

@BindingAdapter("checkAdmin")
fun loadAdmin(view: TextView, boolean: Boolean?) {
    if (boolean == true) {
        view.show()
    } else view.hide()
}

@BindingAdapter("notification")
fun numberNotification(
    view: androidx.appcompat.widget.AppCompatTextView,
    notifications: List<Notification>?
) {
    if (notifications != null && notifications.size > 0) {
        var noRead = notifications?.filter { notification -> notification.status == 0 }
        if (noRead.size<=0) view.hide()
        else {
            view.show()
            if (noRead.size <= 99 && noRead.size>0) {
                view.text = (noRead.size).toString()
            } else {
                view.text = (noRead.size).toString() + "+"
            }
        }

    } else view.hide()
}

@BindingAdapter("follower")
fun numberFollower(view: androidx.appcompat.widget.AppCompatTextView, followers: List<User>?) {
    if (followers != null && followers.size > 0) {
        view.show()
        if (followers.size <= 9)
            view.text = (followers.size).toString()
        else view.text = (followers.size).toString() + "+"
    } else view.hide()
}

fun getDurationAsString(start: Date, end: Date): String? {
    val formatDayOfYear = SimpleDateFormat("dd-MM hh:mm a")
    val formatDate = SimpleDateFormat("dd-MM-YYY")
    val formatTimeOfWeek = SimpleDateFormat("EE hh:mm a")
    val formatTime = SimpleDateFormat("hh:mm a")
    if (start.year == end.year && start.month == end.month && start.day == end.day) {
        return formatTime.format(start)
    } else if (start.year == end.year && start.month == end.month && abs(start.day - end.day) < 7) {
        return formatTimeOfWeek.format(start)
    } else if (start.year == end.year) {
        return formatDayOfYear.format(start)
    } else {
        return formatDate.format(start)
    }
}
