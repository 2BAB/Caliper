package com.bennyhuo.kotlin

import java.text.SimpleDateFormat
import java.util.Date

fun Date.format(format: String): String {
    return SimpleDateFormat(format).format(this)
}

class Hello {
    fun sayHi() {
        println("Hi")
    }
}