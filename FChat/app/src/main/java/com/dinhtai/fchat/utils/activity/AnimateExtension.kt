package com.dinhtai.fchat.utils

import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.transition.Transition
import androidx.transition.TransitionManager.beginDelayedTransition

const val DEFAULT_DURATION = 250L

fun ViewGroup.beginTransition(
    transition: Transition,
    @IdRes vararg viewIds: Int
) {
    beginDelayedTransition(this, transition.apply {
        duration = DEFAULT_DURATION
        viewIds.forEach { addTarget(it) }
    })
}

fun ImageView.changeResource(@DrawableRes resId: Int) {
    ObjectAnimator.ofFloat(this, View.ALPHA, 1.0f, 0.1f).setDuration(DEFAULT_DURATION).start()
    postDelayed({
        setImageResource(resId)
        ObjectAnimator.ofFloat(this, View.ALPHA, 0.1f, 1.0f).setDuration(DEFAULT_DURATION).start()
    }, DEFAULT_DURATION)
}
