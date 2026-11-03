package io.github.ranjandas.kimono.baselineprofile

import android.view.KeyEvent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Walks the screens people use most: home, each workspace, and scrolling the all apps list.
 *
 * It never presses OK and never opens settings, where Left, Right and OK change the theme, so it can't launch an
 * app or change a setting on the device it runs on.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = KIMONO_PACKAGE) {
        startActivityAndWait(homeIntent())
        device.waitForIdle()
        scrollHomeList()
        visitWorkspaces()
        scrollAllApps()
        device.pressBack()
        device.waitForIdle()
    }

    private fun UiDevice.press(keyCode: Int, times: Int = 1) = repeat(times) {
        pressKeyCode(keyCode)
        waitForIdle()
    }

    private fun MacrobenchmarkScope.scrollHomeList() {
        device.press(KeyEvent.KEYCODE_DPAD_DOWN, times = SCROLL_PRESSES)
        device.press(KeyEvent.KEYCODE_DPAD_UP, times = SCROLL_PRESSES)
    }

    // Home's apps list sits at the right edge, so Right leaves the page; the other pages take a press or more.
    private fun MacrobenchmarkScope.visitWorkspaces() {
        device.press(KeyEvent.KEYCODE_DPAD_RIGHT, times = WORKSPACE_PRESSES)
    }

    private fun MacrobenchmarkScope.scrollAllApps() {
        device.press(KeyEvent.KEYCODE_4)
        device.press(KeyEvent.KEYCODE_DPAD_DOWN, times = SCROLL_PRESSES)
        device.press(KeyEvent.KEYCODE_DPAD_UP, times = SCROLL_PRESSES)
    }

    private companion object {
        const val SCROLL_PRESSES = 4
        const val WORKSPACE_PRESSES = 3
    }
}
