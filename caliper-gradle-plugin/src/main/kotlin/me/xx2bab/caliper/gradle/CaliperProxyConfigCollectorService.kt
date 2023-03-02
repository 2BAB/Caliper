package me.xx2bab.caliper.gradle

import me.xx2bab.caliper.gradle.core.CaliperProxyConfigCollector
import me.xx2bab.caliper.gradle.core.GradleKLogger
import me.xx2bab.caliper.gradle.core.ProxyConfig
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logging
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.concurrent.atomic.AtomicBoolean

abstract class CaliperProxyConfigCollectorService
    : BuildService<CaliperProxyConfigCollectorService.Params>, AutoCloseable {
    private val logger = GradleKLogger(Logging.getLogger(CaliperProxyConfigCollectorService::class.java))

    internal interface Params : BuildServiceParameters {}

    lateinit var aggregatedProxyConfig: ProxyConfig
    private val invoked = AtomicBoolean(false)

    override fun close() {
        if (::aggregatedProxyConfig.isInitialized) {
            aggregatedProxyConfig.proxiedClasses.clear()
            aggregatedProxyConfig.proxiedMethods.clear()
            aggregatedProxyConfig.proxiedFields.clear()
        }
    }

    fun collect(fc: FileCollection): ProxyConfig {
        if (invoked.compareAndSet(false, true)) {
            aggregatedProxyConfig = CaliperProxyConfigCollector(logger).doCollect(fc.files) // TODO: can we run them in parallel?
        }
        return aggregatedProxyConfig
    }

}