package com.bennyhuo.kotlin.analyzer

import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import org.junit.Test

/**
 * Created by benny.
 */
class KotlinCodeAnalyzerTest {
    @Test
    fun basic() {
        val result = KotlinCodeAnalyzer(buildOptions {
            inputPaths = listOf("testData")
            inheritClassPath = true
        }).analyze()

        println(result.files)

        result.files.forEach {
            it.declarations.forEach {
                when (it) {
                    is KtNamedFunction -> {
                        println(it.text)
                    }
                    is KtClass -> {
                        println(it.text)
                        println(result.bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, it])
                    }
                }
            }
        }

        // use moduleDescriptor to resolve types from fqname.
        val string = result.moduleDescriptor.resolveClassByFqName(FqName("java.lang.String"), NoLookupLocation.WHEN_FIND_BY_FQNAME)
        println(string)
    }
}