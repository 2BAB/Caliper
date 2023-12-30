package com.bennyhuo.kotlin.analyzer

import io.github.detekt.tooling.api.spec.ProcessingSpec
import io.github.detekt.tooling.dsl.ProcessingSpecBuilder
import java.io.File

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
    var logger: Appendable = System.out

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
                outputChannel = logger
                errorChannel = logger
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