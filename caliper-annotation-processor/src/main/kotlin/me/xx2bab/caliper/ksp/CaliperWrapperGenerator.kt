package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.xx2bab.caliper.anno.*
import me.xx2bab.caliper.common.Constants
import me.xx2bab.caliper.common.ProxiedClass
import me.xx2bab.caliper.common.ProxiedField
import me.xx2bab.caliper.common.ProxiedMethod
import me.xx2bab.caliper.common.toCaliperWrapperFullNameBySlash
import me.xx2bab.caliper.common.toCaliperWrapperSimpleName
import org.apache.commons.text.StringEscapeUtils
import kotlin.text.StringBuilder
import javax.lang.model.element.Modifier

class CaliperWrapperGenerator(
    private val moduleName: String,
    private val metadataMap: Map<String, ProxyMetaData>,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLoggerWrapper
) {

    fun generate() {
        val proxiedMetaData = ProxiedMetaData()
        metadataMap.forEach { (className, metadata) ->
            val simpleClassName = className.split(".").last()
            val wrapperSimpleClassName = simpleClassName.toCaliperWrapperSimpleName()
            val wrapperFullClassNameBySlash = simpleClassName.toCaliperWrapperFullNameBySlash()

            if (metadata.targetClass != null) {
                proxiedMetaData.proxiedClasses.add(
                    ProxiedClass(
                        className = metadata.targetClass!!,
                        replacedClassName = className.replace(".", "/")
                    )
                )

                val classType =
                    TypeSpec.classBuilder(wrapperSimpleClassName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build()
                val javaFile = JavaFile.builder(Constants.CALIPER_PACKAGE_FOR_WRAPPER, classType)
                    .indent("    ")
                    .build()

                val fileOutputStream = codeGenerator.createNewFile(
                    dependencies = Dependencies(false, metadata.sourceRef),
                    packageName = Constants.CALIPER_PACKAGE_FOR_WRAPPER,
                    fileName = wrapperSimpleClassName,
                    extensionName = "java"
                )
                val sb = StringBuilder()
                javaFile.writeTo(sb)
                logger.lifecycle(sb.toString())
                fileOutputStream.write(sb.toString().toByteArray())
                fileOutputStream.close()
            } else {
                val methodSpecs = metadata.methods.map { proxyMethod ->
                    val isAnnotatedWithProxyMethod =
                        proxyMethod.targetType == CaliperMethodProxy::class.simpleName
                    if (isAnnotatedWithProxyMethod) {
                        val pm = ProxiedMethod(
                            className = proxyMethod.targetClassName,
                            methodName = proxyMethod.targetElementName,
                            opcode = proxyMethod.targetOpcode,
                            replacedClassName = wrapperFullClassNameBySlash,
                            replacedMethodName = proxyMethod.methodName
                        )
                        proxiedMetaData.proxiedMethods.add(pm)
                    } else {
                        val pf = ProxiedField(
                            className = proxyMethod.targetClassName,
                            fieldName = proxyMethod.targetElementName,
                            opcode = proxyMethod.targetOpcode,
                            replacedClassName = wrapperFullClassNameBySlash,
                            replacedMethodName = proxyMethod.methodName
                        )
                        proxiedMetaData.proxiedFields.add(pf)
                    }

                    val inputParams = proxyMethod.params.map { methodParam ->
                        ParameterSpec.builder(methodParam.type, methodParam.paramName).build()
                    }

                    val invokeParams = proxyMethod.params.joinToString(",") { it.paramName }
                    logger.lifecycle(proxyMethod.targetClassName)
                    MethodSpec.methodBuilder(proxyMethod.methodName).returns(proxyMethod.returnType)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC).addParameters(inputParams)
                        .addStatement(
                            "// Caliper.visitMethod(\""
                                    + proxyMethod.targetClassName.replace(
                                "$",
                                "$$"
                            ) // https://github.com/square/javapoet/issues/670
                                    + "\",\"${proxyMethod.methodName}\"" + "${if (invokeParams.isBlank()) "" else ","}$invokeParams)"
                        )
                        .addStatement("return $className.${proxyMethod.methodName}($invokeParams)")
                        .build()
                }

                val classType =
                    TypeSpec.classBuilder(wrapperSimpleClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethods(methodSpecs).build()
                val javaFile = JavaFile.builder(Constants.CALIPER_PACKAGE_FOR_WRAPPER, classType)
                    .indent("    ")
                    .build()

                val fileOutputStream = codeGenerator.createNewFile(
                    dependencies = Dependencies(false, metadata.sourceRef),
                    packageName = Constants.CALIPER_PACKAGE_FOR_WRAPPER,
                    fileName = wrapperSimpleClassName,
                    extensionName = "java"
                )
                val sb = StringBuilder()
                javaFile.writeTo(sb)
                logger.lifecycle(sb.toString())
                fileOutputStream.write(sb.toString().toByteArray())
                fileOutputStream.close()
            }

            // TODO: it should link to the wrapper, but we do not find an easy way to refer the wrapper file.
            //   The origin source could work as expect, so we leave it here.
            proxiedMetaData.mapKSFiles.add(metadata.sourceRef)
        }

        val json = Json.encodeToString(proxiedMetaData)
        val jsonFileOutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(true, *proxiedMetaData.mapKSFiles.toTypedArray()),
            packageName = "",
            fileName = "${moduleName.lowercase()}.caliper",
            extensionName = "json"
        )
        jsonFileOutputStream.write(json.toByteArray())
        jsonFileOutputStream.close()
    }

}