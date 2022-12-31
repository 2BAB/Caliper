package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.javapoet.TypeName

data class ProxyMetaData(
    val className: TypeName,
    val targetClassName: String,
    val sourceRef: KSFile,
    var methods: MutableList<ProxyMethod>,
    var properties: MutableList<ProxyProperty>
)

data class ProxyMethod(
    val methodName: String,
    val params: List<MethodParam>,
    val returnType: TypeName?
)

data class ProxyProperty(
    val propertyName: String,
    val type: TypeName
)

data class MethodParam(
    val paramName: String,
    val type: TypeName
)