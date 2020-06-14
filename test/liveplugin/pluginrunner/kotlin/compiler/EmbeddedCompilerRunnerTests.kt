package liveplugin.pluginrunner.kotlin.compiler

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript

@Ignore // Run manually because KtsScriptFixture needs paths to stdlib and kotlin scripting jars
class EmbeddedCompilerRunnerTests {
    @Test fun `can compile an empty file`() = KtsScriptFixture(
        scriptSourceCode = ""
    ).run {
        assertThat(compileScript(), equalTo(emptyList()))
    }

    @Test fun `can compile println`() = KtsScriptFixture(
        scriptSourceCode = "println(123)"
    ).run {
        assertThat(compileScript(), equalTo(emptyList()))
    }

    @Test fun `fails to compile unresolved reference`() = KtsScriptFixture(
        scriptSourceCode = "nonExistingFunction()"
    ).run {
        val errors = compileScript()
        assertThat(errors.size, equalTo(1))
        assertTrue(errors.first().contains("unresolved reference: nonExistingFunction"))
    }
}

private data class KtsScriptFixture(
    val srcDir: File = createTempDir(),
    val outputDir: File = createTempDir(),
    val kotlinStdLibPath: String = System.getenv("kotlin-stdlib-path"),
    val kotlinScriptRuntimePath: String = System.getenv("kotlin-script-runtime-path"),
    val kotlinScriptCommonPath: String = System.getenv("kotlin-script-common-path"),
    val kotlinScriptJvmPath: String = System.getenv("kotlin-script-jvm"),
    val kotlinScriptCompilerEmbeddablePath: String = System.getenv("kotlin-script-compiler-embeddable-path"),
    val kotlinScriptCompilerImplEmbeddablePath: String = System.getenv("kotlin-script-compiler-impl-embeddable-path"),
    val scriptSourceCode: String
) {
    init {
        createTempFile(suffix = ".kts", directory = srcDir).writeText(scriptSourceCode)
    }

    fun compileScript(): List<String> = compile(
        sourceRoot = srcDir.absolutePath,
        classpath = listOf(
            File(kotlinStdLibPath),
            File(kotlinScriptJvmPath),
            File(kotlinScriptCommonPath),
            File(kotlinScriptRuntimePath),
            File(kotlinScriptCompilerEmbeddablePath),
            File(kotlinScriptCompilerImplEmbeddablePath),
            File("build/classes/kotlin/test/")
        ),
        outputDirectory = outputDir,
        kotlinScriptTemplateClass = EmptyKotlinScriptTemplate::class
    )
}

@KotlinScript
class EmptyKotlinScriptTemplate
