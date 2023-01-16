package me.xx2bab.caliper

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import me.xx2bab.caliper.core.CaliperClassVisitor
import me.xx2bab.caliper.core.GradleKLogger
import me.xx2bab.caliper.core.ProxyConfig
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import javax.inject.Inject

interface CaliperClassVisitorFactoryParam : InstrumentationParameters {

    @get:Inject
    @get:Internal
    val gradleLogger: Logger

    @get:Input
    val proxyConfigInJsonString: Property<String>

}

abstract class CaliperClassVisitorFactory : AsmClassVisitorFactory<CaliperClassVisitorFactoryParam> {

    private val proxyConfig = Json.decodeFromString<ProxyConfig>(
        parameters.get().proxyConfigInJsonString.get()
    )

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return CaliperClassVisitor(
            api = Opcodes.ASM9,
            classVisitor = nextClassVisitor,
            config = proxyConfig,
            logger = GradleKLogger(parameters.get().gradleLogger)
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }

}