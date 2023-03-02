package me.xx2bab.caliper.gradle

import com.android.build.api.attributes.BuildTypeAttr
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.google.devtools.ksp.gradle.KspExtension
import me.xx2bab.caliper.common.Constants
import me.xx2bab.caliper.gradle.build.BuildConfig
import me.xx2bab.caliper.gradle.core.GradleKLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.*
import org.gradle.api.tasks.CacheableTask
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import java.util.concurrent.atomic.AtomicBoolean

@CacheableTask
abstract class CaliperPlugin : Plugin<Project> {

    private val androidAppOrLibPluginApplied = AtomicBoolean(false)
    private val annotationProcessorDep = "me.2bab:caliper-annotation-processor:%s"
    private val runtimeDep = "me.2bab:caliper-runtime:%s"

    companion object {
        lateinit var logger: GradleKLogger
    }

    override fun apply(project: Project) {
        project.tasks
        val caliperExtension = project.extensions.create(
            "caliper", CaliperExtension::class.java
        ).apply {
        }
        logger = GradleKLogger(project.logger)

        project.afterEvaluate {
            check(androidAppOrLibPluginApplied.get()) {
                "Caliper plugin should only be applied to an Android Application or Library project " +
                        "but ${project.displayName} doesn't have the 'com.android.application' or 'com.android.library' plugin applied."
            }
        }

        project.dependencies.apply {
            add("implementation", runtimeDep.format(BuildConfig.CALIPER_VERSION))
            add("ksp", annotationProcessorDep.format(BuildConfig.CALIPER_VERSION))
        }


        // `withType<>{}` is null-safe, makes sure if the AppPlugin is not found,
        // the plugin doesn't go through the rest procedure.
        project.plugins.withType<AppPlugin> {
            androidAppOrLibPluginApplied.set(true)

            val caliperConfiguration =
                project.configurations.maybeCreate("caliper").apply {
                    description =
                        "Used by Caliper Gradle Plugin to gather metadata for bytecode transform.."
                    isCanBeConsumed = true
                }
            project.configurations.getByName("implementation")
                .extendsFrom(caliperConfiguration)

            // Mark the current module as an Android Application module
            // to activate meta data aggregation mode.
            project.extensions.getByType<KspExtension>()
                .arg(Constants.KSP_OPTION_ANDROID_APP, "true")

            val androidExtension =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidExtension.onVariants { appVariant ->
                if (!CaliperExtension.isFeatureEnabled(
                        appVariant,
                        caliperExtension.kotlinEnableByVariant,
                        caliperExtension.groovyEnableByVariant
                    )
                ) {
                    return@onVariants
                }

                val capVariantName = appVariant.name.capitalized()
                val artifactType = Attribute.of("artifactType", String::class.java)
                val variantCaliperConfiguration = project.configurations
                    .maybeCreate("${appVariant.name}Caliper")
                    .apply {
                        extendsFrom(caliperConfiguration)
                        isTransitive = false
                        attributes {
                            attribute(
                                BuildTypeAttr.ATTRIBUTE,
                                project.objects.named(
                                    BuildTypeAttr::class.java,
                                    appVariant.buildType.toString()
                                )
                            )
                            attribute(artifactType, "android-java-res")
                        }
                    }

                val proxyConfigCollector = project.gradle
                    .sharedServices
                    .registerIfAbsent(
                        "CaliperProxyConfigCollectorFor${capVariantName}",
                        CaliperProxyConfigCollectorService::class.java
                    ) {}

                appVariant.instrumentation
                    .transformClassesWith(
                        CaliperClassVisitorFactory::class.java,
                        InstrumentationScope.ALL
                    ) {
                        it.variantCaliperConfiguration.from(variantCaliperConfiguration)
                        it.collectorServiceProp.set(proxyConfigCollector)
                    }
                appVariant.instrumentation
                    .setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)

//                val modifyClassesTaskProvider =
//                    project.tasks.register<ModifyClassesTask>("${appVariant.name}ModifyClasses") {
//                        objectFactory = project.objects
//                    }
//                appVariant.artifacts
//                    .forScope(ScopedArtifacts.Scope.ALL)
//                    .use(modifyClassesTaskProvider)
//                    .toTransform(
//                        ScopedArtifact.CLASSES,
//                        ModifyClassesTask::allJars,
//                        ModifyClassesTask::allDirectories,
//                        ModifyClassesTask::output
//                    )
            }
        }

        project.plugins.withType<LibraryPlugin> {
            androidAppOrLibPluginApplied.set(true)
        }
    }


}