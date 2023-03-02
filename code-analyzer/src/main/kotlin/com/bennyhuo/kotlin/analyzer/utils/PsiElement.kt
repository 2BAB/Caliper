package com.bennyhuo.kotlin.analyzer.utils

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils

/**
 * Created by benny.
 */
fun PsiElement.lineAndColumn(): Pair<Int, Int> {
    return PsiDiagnosticUtils.offsetToLineAndColumn(
        containingFile.viewProvider.document,
        textOffset
    ).let {
        it.line to it.column
    }
}