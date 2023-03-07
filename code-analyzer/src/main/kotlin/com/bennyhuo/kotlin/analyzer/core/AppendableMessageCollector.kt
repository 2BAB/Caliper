package com.bennyhuo.kotlin.analyzer.core

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PlainTextMessageRenderer

internal class AppendableMessageCollector(
    private val appendable: Appendable = System.out,
    private val verbose: Boolean = false,
    private val minSeverity: CompilerMessageSeverity = CompilerMessageSeverity.ERROR
) : MessageCollector {
    private val messageRenderer: MessageRenderer = CodeAnalyzerMessageRenderer
    private var hasErrors = false

    override fun clear() {
        // Do nothing, messages are already reported
    }

    override fun report(
        severity: CompilerMessageSeverity,
        message: String,
        location: CompilerMessageSourceLocation?
    ) {
        if (severity.ordinal <= minSeverity.ordinal) {
            if (!verbose && CompilerMessageSeverity.VERBOSE.contains(severity)) return
            hasErrors = hasErrors or severity.isError
            appendable.append(messageRenderer!!.render(severity, message, location))
        }
    }

    override fun hasErrors(): Boolean {
        return hasErrors
    }
}

private object CodeAnalyzerMessageRenderer : PlainTextMessageRenderer() {
    override fun getName() = "code analyzer message renderer"
    override fun getPath(location: CompilerMessageSourceLocation) = location.path
}
