package io.github.ranjandas.kimono.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Cold start without and with the baseline profile, to see what it buys. */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupWithoutProfile() = startup(CompilationMode.None())

    @Test
    fun startupWithProfile() = startup(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun startup(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = KIMONO_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        iterations = ITERATIONS,
        startupMode = StartupMode.COLD,
    ) {
        startActivityAndWait(homeIntent())
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
