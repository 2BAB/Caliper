package me.xx2bab.caliper.gradle

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import me.xx2bab.caliper.gradle.core.CaliperClassVisitor
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

interface CaliperClassVisitorFactoryParam : InstrumentationParameters {
    @get:Classpath
    val variantCaliperConfiguration: ConfigurableFileCollection

    @get:Internal
    val collectorServiceProp: Property<CaliperProxyConfigCollectorService>
}

abstract class CaliperClassVisitorFactory :
    AsmClassVisitorFactory<CaliperClassVisitorFactoryParam> {

    override fun isInstrumentable(classData: ClassData): Boolean {
        return parameters
            .get()
            .collectorServiceProp
            .get()
            .pullTransformExcludedList(parameters.get().variantCaliperConfiguration)
            .contains(classData.className)
            .not()
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val aggregation = parameters.get()
            .collectorServiceProp
            .get()
            .collect(parameters.get().variantCaliperConfiguration)

        return CaliperClassVisitor(
            api = Opcodes.ASM9,
            classVisitor = nextClassVisitor,
            config = aggregation,
            logger = CaliperPlugin.logger
        )
    }

}