package com.bennyhuo.kotlin.analyzer.core

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.PlainTextMessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import java.io.PrintStream

internal class CodeAnalyzerMessageCollector(
    errorStream: PrintStream = System.err,
    verbose: Boolean = false,
    private val minSeverity: CompilerMessageSeverity = CompilerMessageSeverity.ERROR
) : PrintingMessageCollector(errorStream, CodeAnalyzerMessageRenderer, verbose) {
    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        if (severity.ordinal <= minSeverity.ordinal) {
            super.report(severity, message, location)
        }
    }
}

private object CodeAnalyzerMessageRenderer : PlainTextMessageRenderer() {
    override fun getName() = "code analyzer message renderer"
    override fun getPath(location: CompilerMessageSourceLocation) = location.path
}
