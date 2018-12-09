package com.hewking.tinderstack

import android.util.Log

object L {

    @JvmStatic
    fun d(msg : String ,tag : String = "HUILibrary"){
        Log.d(tag,msg)
    }

    @JvmStatic
    fun e(msg : String ,tag : String = "HUILibrary") {
        Log.e(tag,msg)
    }

    @JvmStatic
    fun i(msg : String ,tag : String = "HUILibrary") {
        Log.i(tag,msg)
    }

    @JvmStatic
    fun w(msg : String ,tag : String = "HUILibrary") {
        Log.w(tag,msg)
    }
}