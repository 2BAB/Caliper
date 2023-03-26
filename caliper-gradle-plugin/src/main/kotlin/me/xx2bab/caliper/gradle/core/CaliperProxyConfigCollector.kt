package me.xx2bab.caliper.gradle.core

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.xx2bab.caliper.anno.CaliperFieldProxy
import me.xx2bab.caliper.anno.CaliperMethodProxy
import me.xx2bab.caliper.common.ProxiedClass
import me.xx2bab.caliper.common.ProxiedField
import me.xx2bab.caliper.common.ProxiedMethod
import me.xx2bab.caliper.common.toCaliperWrapperFullNameBySlash
import java.io.File
import java.nio.file.Files
import java.util.jar.JarFile

class CaliperProxyConfigCollector(private val logger: KLogger) {

    private val caliperMetadataFileNameRegex = Regex(""".+\.caliper\.json""")
    private val findVariantRegex = Regex("""library_java_res[/\\](.*?)[/\\]res\.jar""")

    fun doCollect(fc: Set<File>): ProxyConfig {
        val asmOpcodesClassFile = extractASMOpcodesToTempDir() // TODO: lazy init
        val proxyConfig = ProxyConfig()
        fc.forEach { caliperDep ->
            val matchResult = findVariantRegex.find(caliperDep.absolutePath)
            val singleJson = if (matchResult != null) {
                val variantName = matchResult.groupValues[1]
                collectFromSource(caliperDep, variantName, asmOpcodesClassFile.absolutePath)
            } else {
                collectFromBinary(caliperDep)
            }
            if (singleJson != null) {
                proxyConfig.proxiedClasses.addAll(singleJson.proxiedClasses)
                proxyConfig.proxiedMethods.addAll(singleJson.proxiedMethods)
                proxyConfig.proxiedFields.addAll(singleJson.proxiedFields)
            }
        }
        return proxyConfig
    }

    /**
     * For local module.
     */
    private fun collectFromSource(
        file: File,
        variantName: String,
        asmOpcodes: String
    ): ProxyConfig {
        val proxyConfig = ProxyConfig()
        var projectFile = file
        while (projectFile.name != "build") {
            projectFile = projectFile.parentFile
        }
        val projectRootPath = projectFile.parentFile.absolutePath
        logger.info("find local projects which may contain caliper metadata: $projectRootPath with the Variant \"${variantName}\"")

        val sourcePaths = listOf(
            asmOpcodes,
            "${projectRootPath}/src/${variantName}/kotlin",
            "${projectRootPath}/src/${variantName}/groovy",
            "${projectRootPath}/src/${variantName}/java",
            "${projectRootPath}/src/main/kotlin",
            "${projectRootPath}/src/main/groovy",
            "${projectRootPath}/src/main/java",
        ).filter {
            File(it).exists() // TODO: may need to check all sub-directories that at least one file exists
        }
        val s = SimpleAnnotationAnalyzer(logger)
        val start = System.currentTimeMillis()
        s.analyze(sourcePaths, object : SimpleAnnotationAnalyzer.Visitor {
            override fun visitMethodAnnotation(
                annotation: String,
                parameters: Map<String, String>,
                className: String,
                methodName: String
            ) {
                logger.info(
                    "collect Caliper metadata from a method of local project: " +
                            "    annotation = $annotation" +
                            "    params = $parameters" +
                            "    className = $className" +
                            "    methodName = $methodName"
                )
                val simpleClassName = className.split(".").last()
                val fullClassName = className.replace(".", "/")
                if (annotation == CaliperMethodProxy::class.simpleName) {
                    proxyConfig.proxiedMethods.add(
                        ProxiedMethod(
                            targetClassName = parameters["className"]!!,
                            targetMethodName = parameters["methodName"]!!,
                            targetOpcode = parameters["opcode"]!!.toInt(),
                            newClassName = fullClassName,
                            newMethodName = methodName,
                            wrapperClassName = simpleClassName.toCaliperWrapperFullNameBySlash(),
                            wrapperMethodName = methodName
                        )
                    )
                } else if (annotation == CaliperFieldProxy::class.simpleName) {
                    proxyConfig.proxiedFields.add(
                        ProxiedField(
                            targetClassName = parameters["className"]!!,
                            targetFieldName = parameters["fieldName"]!!,
                            targetOpcode = parameters["opcode"]!!.toInt(),
                            newClassName = fullClassName,
                            newMethodName = methodName,
                            wrapperClassName = simpleClassName.toCaliperWrapperFullNameBySlash(),
                            wrapperMethodName = methodName
                        )
                    )
                }
            }

            override fun visitClassAnnotation(
                annotation: String,
                parameters: Map<String, String>,
                className: String
            ) {
                logger.info(
                    "collect Caliper metadata from a class of local project: " +
                            "    annotation = $annotation" +
                            "    params = $parameters" +
                            "    className = $className"
                )
                val fullClassName = className.replace(".", "/")
                proxyConfig.proxiedClasses.add(
                    ProxiedClass(
                        targetClassName = parameters["className"]!!.replace(".", "/"),
                        newClassName = fullClassName
                    )
                )
            }

        })
        val end = System.currentTimeMillis()
        logger.info("CaliperProxyConfigCollector collects proxy config in ${end - start}ms")
        return proxyConfig
    }

    /**
     * For jar package (downloaded remote artifact, extract from .aar).
     */
    @OptIn(ExperimentalSerializationApi::class)
    private fun collectFromBinary(file: File): ProxyConfig? {
        val jar = JarFile(file)
        val entry = jar.entries().asSequence().firstOrNull {
            it.name.matches(caliperMetadataFileNameRegex)
        }
        return if (entry != null) {
            Json.decodeFromStream<ProxyConfig>(jar.getInputStream(entry))
        } else {
            null
        }
    }

    private fun extractASMOpcodesToTempDir(): File {
        val path = "src/main/java/me/xx2bab/caliper/anno"
        val tempDir = Files.createTempDirectory("caliper-annotation-").toFile()
        val packageDir = File(tempDir, path).also { it.mkdirs() }
        val asmOpcodesClassFile = File(packageDir, "ASMOpcodes.java").also { it.createNewFile() }
        val inputStreamOfAsmOpcodes = this::class.java.classLoader
            .getResourceAsStream("ASMOpcodes.java")
        asmOpcodesClassFile.writeBytes(inputStreamOfAsmOpcodes!!.readBytes())
        return asmOpcodesClassFile
    }

}