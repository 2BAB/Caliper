package me.xx2bab.caliper.runtime.record

import android.util.Log

interface AndroidLogger {

    fun i(tag: String, message: String)
    fun d(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String)
    fun v(tag: String, message: String)

}

class DefaultAndroidLogger() : AndroidLogger {

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    override fun v(tag: String, message: String) {
        Log.v(tag, message)
    }

}