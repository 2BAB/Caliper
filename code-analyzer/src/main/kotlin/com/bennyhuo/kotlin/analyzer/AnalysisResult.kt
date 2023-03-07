package com.bennyhuo.kotlin.analyzer

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

/**
 * Created by benny.
 */
data class AnalysisResult(
    val files: List<KtFile>,
    val bindingContext: BindingContext,
    val moduleDescriptor: ModuleDescriptor
)
