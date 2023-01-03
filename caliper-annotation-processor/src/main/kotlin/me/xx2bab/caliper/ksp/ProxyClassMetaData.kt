package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.javapoet.TypeName
import kotlinx.serialization.Transient
import kotlin.reflect.KClass

data class ProxyMetaData(
    val className: TypeName,
    val sourceRef: KSFile,
    var methods: MutableList<ProxyMethod>
)

data class ProxyMethod(
    val methodName: String,
    val params: List<MethodParam>,
    val returnType: TypeName?,
    val targetClassName: String,
    val targetElementName: String, // methodName or fieldName
    val targetOpcode: Int,
    val targetType: String, // CaliperMethodProxy::class.simpleName or CaliperFieldProxy::class.simpleName
)

data class MethodParam(
    val paramName: String,
    val type: TypeName
)