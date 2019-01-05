package com.hewking.tinderswipe

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions

/**
 * 占位符矩形
 */
fun ImageView.load(url: String) {
    get(url).apply(RequestOptions()
        .error(R.drawable.random)
        .placeholder(R.drawable.random)
            .centerCrop()
            .dontAnimate()
            )
            .into(this)
}

fun ImageView.get(url: String): RequestBuilder<Drawable> = Glide.with(context.applicationContext).load(url)
fun ImageView.get(url: Drawable): RequestBuilder<Drawable> = Glide.with(context.applicationContext).load(url)
