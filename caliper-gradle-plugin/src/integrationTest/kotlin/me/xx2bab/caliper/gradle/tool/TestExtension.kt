package me.xx2bab.caliper.gradle.tool

import java.io.File
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.CheckClassAdapter
import java.io.PrintWriter
import java.io.StringWriter

/**
 * A wrap for [CheckClassAdapter] usage.
 *
 * @return Error message or null if everything is good.
 */
fun File.checkByteCodeIntegrity(): String? {
    if (extension != "class") {
        return "The revised file does not come with \".class\" file extension."
    }
    val cr = ClassReader(inputStream())
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    CheckClassAdapter.verify(cr, false, pw)
    val resultLogs = sw.toString()
    // println("[Caliper][Info]: [checkByteCodeIntegrity] $resultLogs")
    return if (resultLogs.contains("AnalyzerException")) {
        resultLogs
    } else {
        null
    }
}