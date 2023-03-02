package com.bennyhuo.kotlin.analyzer

import io.github.detekt.tooling.api.spec.ProcessingSpec
import io.github.detekt.tooling.dsl.ProcessingSpecBuilder
import org.jetbrains.kotlin.js.inline.util.replaceThisReference
import java.io.File
import java.lang.StringBuilder

/**
 * Created by benny.
 */
class Options {
    var jvmTarget: String = "1.8"
    var languageVersion: String? = null
    var classpath: String? = null
    var inheritClassPath: Boolean = false

    var basePath: String? = null
    var inputPaths: Collection<String> = emptyList()
    var debug: Boolean = false

    val clearAppendable = object : Appendable {
        override fun append(csq: CharSequence?): java.lang.Appendable {
            return this
        }

        override fun append(csq: CharSequence?, start: Int, end: Int): java.lang.Appendable {
            return this
        }

        override fun append(c: Char): java.lang.Appendable {
            return this
        }
    }

    internal fun toProcessingSpec(): ProcessingSpec {
        return ProcessingSpecBuilder().apply {
            project {
                inputPaths = this@Options.inputPaths.map {
                    File(it).toPath()
                }
                basePath = this@Options.basePath?.let { File(it).toPath() }
            }

            logging {
                debug = this@Options.debug
                outputChannel = clearAppendable
                errorChannel = clearAppendable
            }

            compiler {
                jvmTarget = this@Options.jvmTarget
                languageVersion = this@Options.languageVersion
                classpath = this@Options.classpath

                if (inheritClassPath) {
                    val currentClassPath = System.getProperty("java.class.path")
                    if (currentClassPath.isNotEmpty()) {
                        classpath = if (classpath.isNullOrBlank()) {
                            currentClassPath
                        } else {
                            "$currentClassPath${File.pathSeparator}$classpath"
                        }
                    }
                }
            }
        }.build()
    }
}

fun buildOptions(init: Options.() -> Unit): Options {
    return Options().apply(init)
}