package com.dinhtai.fchat.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dinhtai.fchat.base.RecyclerItemClickListener

@JvmOverloads
fun RecyclerView.affectOnItemClicks(
    onClick: ((position: Int, view: View) -> Unit)? = null,
    onLongClick: ((position: Int, view: View) -> Unit)? = null
) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, onClick, onLongClick))
}
