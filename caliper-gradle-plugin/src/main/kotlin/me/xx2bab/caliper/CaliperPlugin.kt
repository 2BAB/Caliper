package me.xx2bab.caliper

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@CacheableTask
abstract class CaliperPlugin : Plugin<Project> {

    private val androidAppPluginApplied = AtomicBoolean(false)

    override fun apply(project: Project) {
        val caliperExtension = project.extensions.create(
            "caliper", CaliperExtension::class.java
        ).apply {
        }

        project.afterEvaluate {
            check(androidAppPluginApplied.get()) {
                "Caliper notification plugin should only be applied to an Android Application project " +
                        "but ${project.displayName} doesn't have the 'com.android.application' plugin applied."
            }
        }

        // `withType<>{}` is null-safe, makes sure if the AppPlugin is not found,
        // the plugin doesn't go through the rest procedure.
        project.plugins.withType<AppPlugin> {
            androidAppPluginApplied.set(true)

            val androidExtension = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidExtension.onVariants { appVariant ->
                if (!CaliperExtension.isFeatureEnabled(
                        appVariant,
                        caliperExtension.kotlinEnableByVariant,
                        caliperExtension.groovyEnableByVariant
                    )
                ) {
                    return@onVariants
                }

            }
        }
    }


}

