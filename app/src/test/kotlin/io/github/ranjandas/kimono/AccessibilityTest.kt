package io.github.ranjandas.kimono

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import io.github.ranjandas.kimono.core.data.settings.ClockFormat
import io.github.ranjandas.kimono.core.data.settings.IdleTimeout
import io.github.ranjandas.kimono.core.data.settings.Panels
import io.github.ranjandas.kimono.core.data.settings.TextSize
import io.github.ranjandas.kimono.core.data.workspace.Workspace
import io.github.ranjandas.kimono.core.data.workspace.WorkspaceState
import io.github.ranjandas.kimono.core.design.component.AppOptionsPanel
import io.github.ranjandas.kimono.core.design.component.PinChoice
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme
import io.github.ranjandas.kimono.core.design.theme.ThemeId
import io.github.ranjandas.kimono.data.apps.AppCategory
import io.github.ranjandas.kimono.data.apps.InstalledApp
import io.github.ranjandas.kimono.data.apps.LaunchResult
import io.github.ranjandas.kimono.feature.drawer.DrawerScreen
import io.github.ranjandas.kimono.feature.home.HomeScreen
import io.github.ranjandas.kimono.feature.home.RecentApps
import io.github.ranjandas.kimono.feature.settings.SettingsScreen
import io.github.ranjandas.kimono.feature.settings.WallpaperChoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What TalkBack gets from each screen (#45): everything it can focus has a name, and none of it is an icon glyph,
 * since icons are decorative and the label next to them carries the meaning.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w960dp-h540dp-land-xhdpi")
class AccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `home names everything it can focus`() = assertAccessible {
        HomeScreen(
            apps = SampleApps,
            onLaunchApp = { LaunchResult.Launched },
            workspaces = WorkspaceState(pins = mapOf(Workspace.Home to listOf("pkg.Jellyfin"))),
            onSelectWorkspace = {},
            onTogglePin = { _, _ -> },
            onMovePin = { _, _, _ -> },
            onOpenAppInfo = {},
            onUninstallApp = {},
            theme = ThemeId.Default,
            onOpenSettings = {},
            clockFormat = ClockFormat.TwentyFourHour,
            recents = RecentApps.Apps(SampleApps.take(3)),
            onRequestUsageAccess = {},
            allApps = {},
        )
    }

    @Test
    fun `all apps names everything it can focus`() = assertAccessible {
        DrawerScreen(
            apps = SampleApps,
            onLaunchApp = { LaunchResult.Launched },
            workspaces = WorkspaceState(),
            onTogglePin = { _, _ -> },
            onOpenAppInfo = {},
            onUninstallApp = {},
            onClose = {},
        )
    }

    @Test
    fun `app options name everything they can focus`() = assertAccessible {
        AppOptionsPanel(
            appLabel = "Jellyfin",
            pins = listOf(PinChoice("home", pinned = true), PinChoice("watch", pinned = false)),
            onTogglePin = {},
            onOpenAppInfo = {},
            onUninstall = {},
            onDismiss = {},
            onMoveUp = {},
            onMoveDown = {},
            iconStyle = "logo",
            onNextIconStyle = {},
        )
    }

    @Test
    fun `settings name everything they can focus, with each value as its state`() {
        assertAccessible { Settings() }
        val theme = clickable().single { it.label() == "theme" }
        assertEquals("kimono", theme.config.getOrNull(SemanticsProperties.StateDescription))
        val actions = theme.config.getOrNull(SemanticsActions.CustomActions).orEmpty().map { it.label }
        assertEquals(listOf("previous value", "next value"), actions)
        assertEquals("theme", theme.label())
    }

    private fun assertAccessible(content: @Composable () -> Unit) {
        compose.setContent { KimonoTheme(ThemeId.Default, CornerStyle.Default) { content() } }
        val nodes = clickable()
        assertTrue("nothing to focus", nodes.isNotEmpty())
        nodes.forEach { node ->
            val label = node.label()
            assertTrue("a focusable element has no name: ${node.config}", label.isNotBlank())
            assertTrue("an icon glyph is read out in \"$label\"", label.none(::isIconGlyph))
        }
        allText().forEach { text -> assertTrue("an icon glyph is read out in \"$text\"", text.none(::isIconGlyph)) }
    }

    private fun clickable(): List<SemanticsNode> =
        compose.onRoot().fetchSemanticsNode().allDescendants().filter { SemanticsActions.OnClick in it.config }

    private fun allText(): List<String> =
        compose.onRoot().fetchSemanticsNode().allDescendants().flatMap { it.label().lines() }

    @Composable
    private fun Settings() = SettingsScreen(
        theme = ThemeId.Kimono,
        onThemeChange = {},
        cornerStyle = CornerStyle.Default,
        onCornerStyleChange = {},
        clockFormat = ClockFormat.System,
        onClockFormatChange = {},
        textSize = TextSize.Default,
        onTextSizeChange = {},
        idleTimeout = IdleTimeout.Default,
        onIdleTimeoutChange = {},
        pixelShift = true,
        onPixelShiftChange = {},
        wallpaper = WallpaperChoice.Theme,
        onWallpaperToggle = {},
        panels = Panels.Default,
        onPanelsChange = {},
        onClose = {},
    )

    private companion object {
        val SampleApps = listOf(
            "Jellyfin" to AppCategory.Video,
            "Plex" to AppCategory.Video,
            "Spotify" to AppCategory.Audio,
            "Retro" to AppCategory.Game,
            "Files" to AppCategory.Other,
        ).map { (label, category) -> InstalledApp("pkg.$label", "pkg.$label.Main", label, category, isTvApp = true) }
    }
}

/** What TalkBack reads for a node: its content description, else its text, joined across merged children. */
private fun SemanticsNode.label(): String {
    val described = config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
    val text = config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
    return (described + text).joinToString("\n")
}

private fun SemanticsNode.allDescendants(): List<SemanticsNode> = children.flatMap { listOf(it) + it.allDescendants() }

// The icon font's glyphs all sit in Unicode's private use areas.
private fun isIconGlyph(c: Char) = c in ''..'' || c.isSurrogate()
