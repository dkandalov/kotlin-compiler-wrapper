package liveplugin.pluginrunner.kotlin.compiler

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.io.File

@Ignore // Run manually because gradle won't have "kotlin_stdlib_path".
class EmbeddedCompilerRunnerTests {
    @Test fun `can compile an empty file`() =
        KtsScriptFixture(
            scriptSourceCode = ""
        ).run {
            assertThat(compile(), equalTo(emptyList()))
        }

    @Test fun `can compile println`() =
        KtsScriptFixture(
            scriptSourceCode = "println(123)"
        ).run {
            assertThat(compile(), equalTo(emptyList()))
        }

    @Test fun `fails to compile unresolved reference`() =
        KtsScriptFixture(
            scriptSourceCode = "nonExistingFunction()"
        ).run {
            val errors = compile()
            assertThat(errors.size, equalTo(1))
            assertTrue(errors.first().contains("unresolved reference: nonExistingFunction"))
        }
}

data class KtsScriptFixture(
    val srcDir: File = createTempDir(),
    val outputDir: File = createTempDir(),
    val kotlinStdLibPath: String = System.getenv("kotlin_stdlib_path"),
    val scriptSourceCode: String
) {
    init {
        createTempFile(suffix = ".kts", directory = srcDir).writeText(scriptSourceCode)
    }

    fun compile(): List<String> = compile(
        sourceRoot = srcDir.absolutePath,
        classpath = listOf(File(kotlinStdLibPath), File("build/classes/kotlin/test/")),
        outputDirectory = outputDir,
        kotlinScriptTemplateClass = EmptyKotlinScriptTemplate::class.java
    )

    class EmptyKotlinScriptTemplate
}
