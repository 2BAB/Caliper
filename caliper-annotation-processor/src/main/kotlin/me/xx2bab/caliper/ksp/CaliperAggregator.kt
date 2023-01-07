package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.xx2bab.caliper.anno.CaliperMeta
import me.xx2bab.caliper.common.Constants

class CaliperAggregator(
    private val logger: KSPLoggerWrapper
) {
    @OptIn(KspExperimental::class)
    fun collect(aggregatedMetadata: ProxiedMetaData, resolver: Resolver) {
        logger.info("Query all sub projects meta data")
        resolver.getDeclarationsFromPackage(Constants.CALIPER_PACKAGE_FOR_WRAPPER)
            .filter {
                it is KSClassDeclaration
                        && it.annotations.any { anno -> anno.shortName.asString() == CaliperMeta::class.simpleName }
            }
            .forEach {
                logger.info("Aggregate from ${it.qualifiedName?.asString()}")
                it.containingFile?.let { aggregatedMetadata.mapKSFiles.add(it) }
                val meta = it.annotations.first { anno ->
                    anno.shortName.asString() == CaliperMeta::class.simpleName
                }
                val subMetaData = Json.decodeFromString<ProxiedMetaData>(
                    meta.arguments.first().value.toString()
                )
                aggregatedMetadata.proxiedMethods.addAll(subMetaData.proxiedMethods)
                aggregatedMetadata.proxiedFields.addAll(subMetaData.proxiedFields)
            }
    }

    fun generate(aggregatedMetadata: ProxiedMetaData, codeGenerator: CodeGenerator) {
        val os = codeGenerator.createNewFile(
            Dependencies(true, *aggregatedMetadata.mapKSFiles.toTypedArray()),
            packageName = "",
            fileName = Constants.CALIPER_AGGREGATE_METADATA_FILE_NAME,
            extensionName = "json"
        )
        os.write(Json.encodeToString(aggregatedMetadata).toByteArray())
        os.close()
    }

}