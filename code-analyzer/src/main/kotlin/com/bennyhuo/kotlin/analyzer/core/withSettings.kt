@file:Suppress("INVISIBLE_REFERENCE")

package com.bennyhuo.kotlin.analyzer.core

import io.github.detekt.tooling.api.spec.ProcessingSpec
import io.github.detekt.utils.openSafeStream
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import io.gitlab.arturbosch.detekt.core.baseline.DETEKT_BASELINE_CREATION_KEY
import io.gitlab.arturbosch.detekt.core.baseline.DETEKT_BASELINE_PATH_KEY
import io.gitlab.arturbosch.detekt.core.config.CompositeConfig
import io.gitlab.arturbosch.detekt.core.config.YamlConfig
import io.gitlab.arturbosch.detekt.core.reporting.DETEKT_OUTPUT_REPORT_BASE_PATH_KEY
import io.gitlab.arturbosch.detekt.core.reporting.DETEKT_OUTPUT_REPORT_PATHS_KEY
import io.gitlab.arturbosch.detekt.core.tooling.getDefaultConfiguration
import java.net.URL
import java.nio.file.Path

internal fun ProcessingSpec.toSettings(): ProcessingSettings {
    val configuration = loadConfiguration()
    val settings =
        ProcessingSettings(this, configuration).apply {
            baselineSpec.path?.let { register(DETEKT_BASELINE_PATH_KEY, it) }
            register(DETEKT_BASELINE_CREATION_KEY, baselineSpec.shouldCreateDuringAnalysis)
            register(DETEKT_OUTPUT_REPORT_PATHS_KEY, reportsSpec.reports)
            projectSpec.basePath?.let { register(DETEKT_OUTPUT_REPORT_BASE_PATH_KEY, it) }
        }
    return settings
}

internal fun ProcessingSpec.loadConfiguration(): Config = with(configSpec) {
    var declaredConfig: Config? = when {
        configPaths.isNotEmpty() -> parsePathConfig(configPaths)
        resources.isNotEmpty() -> parseResourceConfig(resources)
        else -> null
    }

    if (useDefaultConfig) {
        declaredConfig = if (declaredConfig == null) {
            getDefaultConfiguration()
        } else {
            CompositeConfig(
                declaredConfig, getDefaultConfiguration()
            )
        }
    }

    return declaredConfig ?: getDefaultConfiguration()

}

private fun parseResourceConfig(urls: Collection<URL>): Config =
    if (urls.size == 1) {
        urls.first().openSafeStream().reader().use(YamlConfig::load)
    } else {
        urls.asSequence()
            .map { it.openSafeStream().reader().use(YamlConfig::load) }
            .reduce { composite, config -> CompositeConfig(config, composite) }
    }

private fun parsePathConfig(paths: Collection<Path>): Config =
    if (paths.size == 1) {
        YamlConfig.load(paths.first())
    } else {
        paths.asSequence()
            .map { YamlConfig.load(it) }
            .reduce { composite, config -> CompositeConfig(config, composite) }
    }