package liveplugin.pluginrunner.kotlin.compiler

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.ERROR
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.EXCEPTION
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer.PLAIN_FULL_PATHS
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles.JVM_CONFIG_FILES
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CommonConfigurationKeys.MODULE_NAME
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys.*
import org.jetbrains.kotlin.config.KotlinSourceRoot
import org.jetbrains.kotlin.script.KotlinScriptDefinition
import java.io.File
import kotlin.jvm.internal.Reflection


@Suppress("unused")
fun compilePlugin(
    sourceRoot: String,
    classpath: List<File>,
    compilerOutput: File,
    kotlinScriptTemplateClass: Class<*>
): List<String> {
    val rootDisposable = Disposer.newDisposable()

    try {
        val messageCollector = ErrorMessageCollector()
        val configuration = createCompilerConfiguration(sourceRoot, classpath, compilerOutput, messageCollector, kotlinScriptTemplateClass)
        val kotlinEnvironment = KotlinCoreEnvironment.createForProduction(rootDisposable, configuration, JVM_CONFIG_FILES)
        val state = KotlinToJVMBytecodeCompiler.analyzeAndGenerate(kotlinEnvironment)

        return when {
            messageCollector.hasErrors() -> messageCollector.errors
            state == null -> listOf("Compiler returned empty state.")
            else -> emptyList()
        }
    } finally {
        rootDisposable.dispose()
    }
}

private class ErrorMessageCollector : MessageCollector {
    val errors = ArrayList<String>()

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation?) {
        if (severity == ERROR || severity == EXCEPTION) {
            errors.add(PLAIN_FULL_PATHS.render(severity, message, location))
        }
    }

    override fun clear() {
        errors.clear()
    }

    override fun hasErrors() = errors.isNotEmpty()
}


private fun createCompilerConfiguration(
    sourceRoot: String,
    classpath: List<File>,
    compilerOutput: File,
    messageCollector: MessageCollector,
    kotlinScriptTemplateClass: Class<*>
): CompilerConfiguration {
    return CompilerConfiguration().apply {
        put(MODULE_NAME, "LivePluginScript")
        put(MESSAGE_COLLECTOR_KEY, messageCollector)
        add(SCRIPT_DEFINITIONS, KotlinScriptDefinition(Reflection.createKotlinClass(kotlinScriptTemplateClass)))

        add(CONTENT_ROOTS, KotlinSourceRoot(sourceRoot))

        for (path in classpath) {
            add(CONTENT_ROOTS, JvmClasspathRoot(path))
        }

        put(RETAIN_OUTPUT_IN_MEMORY, false)
        put(OUTPUT_DIRECTORY, compilerOutput)
    }
}

