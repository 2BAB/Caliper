package me.xx2bab.caliper.gradle.core

import com.bennyhuo.kotlin.analyzer.KotlinCodeAnalyzer
import com.bennyhuo.kotlin.analyzer.buildOptions
import org.apache.commons.text.StringEscapeUtils
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall


class SimpleAnnotationAnalyzer(private val logger: KLogger) {

    private val appendableLogger = object: Appendable {
        override fun append(csq: CharSequence?): java.lang.Appendable {
            logger.debug(csq.toString())
            return this
        }

        override fun append(csq: CharSequence?, start: Int, end: Int): java.lang.Appendable {
            logger.debug(csq.toString())
            return this
        }

        override fun append(c: Char): java.lang.Appendable {
            logger.debug(c.toString())
            return this
        }
    }

    fun analyze(
        sources: List<String>,
        visitor: Visitor
    ) {
        logger.info("SimpleAnnotationAnalyzer: starting...")
        val result = KotlinCodeAnalyzer(buildOptions {
            inputPaths = sources
            inheritClassPath = false
            logger = appendableLogger
        }).analyze()
        logger.info("SimpleAnnotationAnalyzer processed: ${result.files}")
        result.files.forEach {
            val packageName = it.packageFqName.asString()
            it.declarations.forEach { decl ->
                if (decl is KtObjectDeclaration) {
                    val obj = decl
                    val className = obj.nameAsSafeName.asString()
                    obj.annotationEntries.forEach { anno ->
                        val parameters =
                            parseAnnotationArguments(anno.valueArguments, result.bindingContext)
                        visitor.visitClassAnnotation(
                            anno.shortName!!.asString(),
                            parameters,
                            "$packageName.$className"
                        )
                    }
                    obj.body?.functions?.forEach { func ->
                        val funcName = func.nameAsSafeName.asString()
                        func.annotationEntries.forEach { anno ->
                            val parameters =
                                parseAnnotationArguments(anno.valueArguments, result.bindingContext)
                            visitor.visitMethodAnnotation(
                                anno.shortName!!.asString(),
                                parameters,
                                "$packageName.$className",
                                funcName
                            )
                        }
                    }
                }
            }
        }
    }

    private fun parseAnnotationArguments(
        argList: List<ValueArgument>,
        bindingContext: BindingContext
    ): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        argList.forEach { valueArg ->
            val rawString = StringEscapeUtils.unescapeJava(
                valueArg.getArgumentExpression()!!
                    .text.replace("\"", "")
            )
            parameters[valueArg.getArgumentName()!!.asName.asString()] =
                valueArg.getArgumentExpression()
                    .getResolvedCall(bindingContext)
                    .let { resolvedCall ->
                        if (resolvedCall == null) {
                            rawString
                        } else {
                            (resolvedCall.resultingDescriptor as PropertyDescriptor)
                                .compileTimeInitializer.toString().replace("\"", "")
                        }
                    }

        }
        return parameters
    }


    interface Visitor {

        fun visitMethodAnnotation(
            annotation: String,
            parameters: Map<String, String>,
            className: String,
            methodName: String
        )

        fun visitClassAnnotation(
            annotation: String,
            parameters: Map<String, String>,
            className: String
        )

    }


}
