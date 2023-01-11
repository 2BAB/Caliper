package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

data class ProxyMetaData(
    val classTypeName: TypeName,
    val sourceRef: KSFile,
    var methods: MutableList<ProxyMethod>,
    var targetClass: String?
)

data class ProxyMethod(
    val methodName: String,
    val params: List<MethodParam>,
    val modifiers: List<Modifier>,
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