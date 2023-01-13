package me.xx2bab.caliper.runtime.record

import android.app.Application
import android.os.Build
import android.os.Looper
import me.xx2bab.caliper.runtime.SignatureVisitor
import android.os.Process
import me.xx2bab.caliper.runtime.str.Strings

internal const val MAIN_TAG = "[Caliper] "

class CaliperLogger(private val logger: AndroidLogger = DefaultAndroidLogger()) : SignatureVisitor {


    private val mainHead = "Proxy triggered: "
    private val subHead = "    --> "

    override fun visit(
        className: String,
        elementName: String,
        parameterNames: Array<String>,
        parameterValues: Array<Any>
    ) {
        val sb = StringBuilder()

        // title
        sb.append(mainHead)
        sb.append("class \"")
        sb.append(className)
        sb.append("\" with element \"")
        sb.append(elementName)
        sb.append("\" :\n")

        // Process
        sb.append(subHead)
        sb.append("process identifier: ")
        sb.append(getProcessIdentifier())
        sb.append("\n")

        // Thread
        sb.append(subHead)
        sb.append("thread: ")
        if (Looper.myLooper() == Looper.getMainLooper()) {
            sb.append("main thread")
        } else {
            sb.append(Thread.currentThread().name)
        }
        sb.append("\n")

        // Parameters
        sb.append(subHead)
        sb.append("parameters: ")
        for (i in parameterNames.indices) {
            sb.append(parameterNames[i])
            sb.append(" = ")
            sb.append(Strings.toString(parameterValues[i]))
            sb.append(", ")
        }

        // Print
        logger.d(MAIN_TAG, sb.toString())
    }


    private fun getProcessIdentifier(): String {
        return if (Build.VERSION.SDK_INT >= 28) {
            Application.getProcessName()
        } else {
            Process.myPid().toString()
        }
    }

}