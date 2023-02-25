package me.xx2bab.caliper.gradle

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.xx2bab.caliper.gradle.core.CaliperClassVisitor
import me.xx2bab.caliper.gradle.core.ProxyConfig
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Classpath
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.util.jar.JarFile

interface CaliperClassVisitorFactoryParam : InstrumentationParameters {
    @get:Classpath
    val variantCaliperConfiguration: ConfigurableFileCollection
}

abstract class CaliperClassVisitorFactory :
    AsmClassVisitorFactory<CaliperClassVisitorFactoryParam> {


    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val aggregation = ProxyConfig()
        parameters.get().variantCaliperConfiguration.forEach { caliperDep ->
            val jar = JarFile(caliperDep)
            val entry = jar.entries().asSequence().firstOrNull {
                it.name == "aggregation.caliper.json"
            }
            if (entry != null) {
                val singleJson = Json.decodeFromStream<ProxyConfig>(jar.getInputStream(entry))
                aggregation.proxiedClasses.addAll(singleJson.proxiedClasses)
                aggregation.proxiedMethods.addAll(singleJson.proxiedMethods)
                aggregation.proxiedFields.addAll(singleJson.proxiedFields)
            }
        }

        CaliperPlugin.logger.info("Aggregated Config: ${Json.encodeToString(aggregation)}")

        return CaliperClassVisitor(
            api = Opcodes.ASM9,
            classVisitor = nextClassVisitor,
            config = aggregation,
            logger = CaliperPlugin.logger
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
//        return classData.className.startsWith("me.xx2bab.caliper.sample.MainActivity")
        return (classData.className.startsWith("me.xx2bab.caliper.runtime")
                || classData.className.startsWith("me.xx2bab.caliper.sample.customproxy")).not()
    }

}