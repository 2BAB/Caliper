package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier
import kotlin.text.StringBuilder

class CaliperWrapperGenerator(
    private val metadataMap: Map<String, ProxyMetaData>,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLoggerWrapper
) {

    fun generate() {
        metadataMap.forEach { (className, metadata) ->
            val simpleClassName = className.split(".").last().toCaliperWrapperName()
            val methodSpecs = metadata.methods.map { proxyMethod ->
                val inputParams = proxyMethod.params.map { methodParam ->
                    ParameterSpec.builder(
                        methodParam.type,
                        methodParam.paramName
                    ).build()
                }

                val invokeParams = proxyMethod.params.joinToString(",") { it.paramName }
                logger.lifecycle(metadata.targetClassName)
                MethodSpec.methodBuilder(proxyMethod.methodName)
                    .returns(proxyMethod.returnType)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameters(inputParams)
                    .addStatement("// Caliper.visitMethod(\""
                            + metadata.targetClassName.replace("$", "$$")
                            + "\",\"${proxyMethod.methodName}\",$invokeParams)") // https://github.com/square/javapoet/issues/670
                    .addStatement("return $className.${proxyMethod.methodName}($invokeParams)")
                    .build()
            }

            val classType = TypeSpec.classBuilder(simpleClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .also { classBuilder ->
                    classBuilder.addMethods(methodSpecs)
                }
                .build()
            val javaFile = JavaFile.builder(Constants.CALIPER_PACKAGE_FOR_WRAPPER, classType)
                .indent("    ")
                .build()

            val fileOutputStream = codeGenerator.createNewFile(
                dependencies = Dependencies(false, metadata.sourceRef),
                packageName = Constants.CALIPER_PACKAGE_FOR_WRAPPER,
                fileName = simpleClassName,
                extensionName = "java"
            )
            val sb = StringBuilder()
            javaFile.writeTo(sb)
            logger.lifecycle(sb.toString())
            fileOutputStream.write(sb.toString().toByteArray())
        }
    }

    private fun String.toCaliperWrapperName() = this + "_CaliperWrapper"

}