package com.bennyhuo.kotlin.analyzer.core

import com.bennyhuo.kotlin.analyzer.AnalysisResult
import io.gitlab.arturbosch.detekt.core.KtTreeCompiler
import io.gitlab.arturbosch.detekt.core.ProcessingSettings
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory

internal class InternalAnalyzer(
    private val settings: ProcessingSettings
) {

    fun run(): AnalysisResult {
        val compiler = KtTreeCompiler(settings, settings.spec.projectSpec)
        val filesToAnalyze = settings.spec.projectSpec.inputPaths.flatMap(compiler::compile)
        return doAnalyze(settings.environment, filesToAnalyze)
    }

    private fun doAnalyze(
        environment: KotlinCoreEnvironment,
        files: List<KtFile>,
    ): AnalysisResult {
        val analyzer = AnalyzerWithCompilerReport(
            CodeAnalyzerMessageCollector(minSeverity = CompilerMessageSeverity.ERROR),
            environment.configuration.languageVersionSettings,
            true
        )
        analyzer.analyzeAndReport(files) {
            TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                environment.project,
                files,
                NoScopeRecordCliBindingTrace(),
                environment.configuration,
                environment::createPackagePartProvider,
                ::FileBasedDeclarationProviderFactory
            )
        }

        return AnalysisResult(files, analyzer.analysisResult.bindingContext, analyzer.analysisResult.moduleDescriptor)
    }

}