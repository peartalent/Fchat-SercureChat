package com.dinhtai.fchat.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseViewHolder<T, B : ViewBinding>(
    binding: B,
    onItemClick: (T) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private var itemData: T? = null

    init {
        binding.root.setOnClickListener { view->
            itemData?.let {
                onItemClick(it)
            }
        }
    }

    open fun onBind(itemData: T) {
        this.itemData = itemData
    }
}
