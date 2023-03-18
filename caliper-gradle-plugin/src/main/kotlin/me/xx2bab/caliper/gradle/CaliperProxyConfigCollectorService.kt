package me.xx2bab.caliper.gradle

import me.xx2bab.caliper.gradle.core.CaliperProxyConfigCollector
import me.xx2bab.caliper.gradle.core.GradleKLogger
import me.xx2bab.caliper.gradle.core.ProxyConfig
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logging
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class CaliperProxyConfigCollectorService
    : BuildService<CaliperProxyConfigCollectorService.Params>, AutoCloseable {
    private val logger =
        GradleKLogger(Logging.getLogger(CaliperProxyConfigCollectorService::class.java))

    internal interface Params : BuildServiceParameters {}

    lateinit var aggregatedProxyConfig: ProxyConfig
    lateinit var excludeClassList: MutableSet<String>

    init {
        logger.info(
            "CaliperProxyConfigCollectorService is initialized.\n" +
                    "aggregatedProxyConfig: ${::aggregatedProxyConfig.isInitialized}"
        )
    }

    override fun close() {
        if (::aggregatedProxyConfig.isInitialized) {
            aggregatedProxyConfig.proxiedClasses.clear()
            aggregatedProxyConfig.proxiedMethods.clear()
            aggregatedProxyConfig.proxiedFields.clear()
        }
    }

    fun collect(fc: FileCollection): ProxyConfig {
        synchronized(this) {
            if (::aggregatedProxyConfig.isInitialized.not()) {
                val start = System.currentTimeMillis()
                aggregatedProxyConfig =
                    CaliperProxyConfigCollector(logger).doCollect(fc.files) // TODO: run them in parallel
                val end = System.currentTimeMillis()
                logger.info("CaliperProxyConfigCollectorService collects proxy config in ${end - start}ms")
                logger.info("Aggregated Proxy Config: $aggregatedProxyConfig")
            }
        }
        return aggregatedProxyConfig
    }

    fun pullTransformExcludedList(fc: FileCollection): Set<String> {
        synchronized(this) {
            if (::excludeClassList.isInitialized.not()) {
                excludeClassList = mutableSetOf()
                collect(fc)
                aggregatedProxyConfig.proxiedClasses.forEach {
                    excludeClassList.add(it.newClassName.replace("/", "."))
                }
                aggregatedProxyConfig.proxiedMethods.forEach {
                    excludeClassList.add(it.newClassName.replace("/", "."))
                }
                aggregatedProxyConfig.proxiedFields.forEach {
                    excludeClassList.add(it.newClassName.replace("/", "."))
                }
                logger.info("Generate Transform-Exclude List: $excludeClassList")
            }
        }
        return excludeClassList
    }
}