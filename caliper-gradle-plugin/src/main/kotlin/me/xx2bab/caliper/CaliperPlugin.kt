package me.xx2bab.caliper

import me.xx2bab.caliper.gradle.build.BuildConfig
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import me.xx2bab.caliper.common.Constants.CALIPER_AGGREGATE_METADATA_FILE_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.CacheableTask
import org.gradle.kotlin.dsl.withType
import java.util.concurrent.atomic.AtomicBoolean

@CacheableTask
abstract class CaliperPlugin : Plugin<Project> {

    private val androidAppOrLibPluginApplied = AtomicBoolean(false)
    private val annotationProcessorDep = "me.2bab:caliper-annotation-processor:%s"
    private val runtimeDep = "me.2bab:caliper-annotation-processor:%s"

    override fun apply(project: Project) {
        val caliperExtension = project.extensions.create(
            "caliper", CaliperExtension::class.java
        ).apply {
        }

        project.afterEvaluate {
            check(androidAppOrLibPluginApplied.get()) {
                "Caliper plugin should only be applied to an Android Application or Library project " +
                        "but ${project.displayName} doesn't have the 'com.android.application' or 'com.android.library' plugin applied."
            }
        }

        project.dependencies.apply {
            add("implementation", annotationProcessorDep.format(BuildConfig.CALIPER_VERSION))
            add("ksp", runtimeDep.format(BuildConfig.CALIPER_VERSION))
        }

        // `withType<>{}` is null-safe, makes sure if the AppPlugin is not found,
        // the plugin doesn't go through the rest procedure.
        project.plugins.withType<AppPlugin> {
            androidAppOrLibPluginApplied.set(true)

            val aggregatedConfigInJsonString = project.layout.buildDirectory
                .file("generated/ksp/main/resources/$CALIPER_AGGREGATE_METADATA_FILE_NAME.json")
                .map { it.asFile.readText() }
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

                appVariant.instrumentation
                    .transformClassesWith(
                        CaliperClassVisitorFactory::class.java,
                        InstrumentationScope.ALL
                    ) {
                        it.proxyConfigInJsonString.set(aggregatedConfigInJsonString)
                    }
                appVariant.instrumentation
                    .setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
            }
        }

        project.plugins.withType<LibraryPlugin> {
            androidAppOrLibPluginApplied.set(true)
        }
    }


}


