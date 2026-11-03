package io.github.ranjandas.kimono.feature.drawer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.component.FocusDefaults
import io.github.ranjandas.kimono.core.design.component.TileDefaults
import io.github.ranjandas.kimono.core.design.component.requestFocusWhenLaidOut
import io.github.ranjandas.kimono.core.design.icon.Icon
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.text.count
import io.github.ranjandas.kimono.core.design.theme.KimonoSpacing
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * The search line at the top of all apps, as a real text field so the on-screen keyboard and the Google TV phone
 * app's keyboard can type into it. Moving onto it with the D-pad only highlights the line: a text field brings up
 * the keyboard as soon as it has focus, and the OK that follows would then press a key on it. So the line takes
 * focus itself, and only OK, on its release, hands focus to the field and brings up the keyboard. The keyboard's
 * search key ([onDone]) goes back to the list. [onFocusChange] says when the line or the field has focus, and
 * [focusRequester] focuses the line.
 */
@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    matches: Int,
    onFocusChange: (Boolean) -> Unit,
    onDone: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val lineSource = remember { MutableInteractionSource() }
    val fieldSource = remember { MutableInteractionSource() }
    val lineFocused by lineSource.collectIsFocusedAsState()
    val fieldFocused by fieldSource.collectIsFocusedAsState()
    val focused = lineFocused || fieldFocused
    var typing by remember { mutableStateOf(false) }
    val fieldFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = KimonoTheme.colors
    val focusChange by rememberUpdatedState(onFocusChange)
    LaunchedEffect(focused) { focusChange(focused) }
    LaunchedEffect(typing) {
        if (typing) {
            fieldFocus.requestFocusWhenLaidOut()
            keyboard?.show()
        }
    }
    // Once the field lets go of focus (the keyboard's search key, Back), the next OK starts typing again.
    LaunchedEffect(fieldFocused) { if (!fieldFocused) typing = false }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
                val ok = event.key in OkKeys && !typing
                if (ok && event.type == KeyEventType.KeyUp) typing = true
                ok
            }
            // After the key handler, so it sees OK while the line itself has focus.
            .focusRequester(focusRequester)
            .focusable(interactionSource = lineSource),
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("~/apps ❯", style = KimonoTheme.typography.headline, color = colors.accent)
        SearchBox(focused, Modifier.weight(1f)) {
            SearchInput(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = placeholder(lineFocused, typing),
                onSearch = {
                    keyboard?.hide()
                    onDone()
                },
                interactionSource = fieldSource,
                modifier = Modifier.weight(1f).focusRequester(fieldFocus).focusProperties { canFocus = typing },
            )
        }
        Text(count(matches, "app"), style = KimonoTheme.typography.label, color = colors.textMuted)
    }
}

/**
 * The field's box: a quiet fill and border with a magnifier, so it reads as somewhere to type rather than a heading
 * (#133). Focus eases the border to the accent, as on a tile.
 */
@Composable
private fun SearchBox(focused: Boolean, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val colors = KimonoTheme.colors
    val shape = KimonoTheme.shapes.nested(TileDefaults.PADDING_DP)
    val fill by animateColorAsState(
        if (focused) colors.surfaceVariant else colors.surfaceVariant.copy(alpha = RESTING_FILL_ALPHA),
        tween(FocusDefaults.DURATION_MS),
        label = "search fill",
    )
    val border by animateColorAsState(
        if (focused) colors.borderFocused else colors.border.copy(alpha = FocusDefaults.QUIET_BORDER_ALPHA),
        tween(FocusDefaults.DURATION_MS),
        label = "search border",
    )
    Row(
        modifier = modifier
            .background(fill, shape)
            .border(TileDefaults.BorderWidth, border, shape)
            .padding(horizontal = KimonoSpacing.M, vertical = KimonoSpacing.S),
        horizontalArrangement = Arrangement.spacedBy(KimonoSpacing.S),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(KimonoIcon.Search, color = if (focused) colors.accent else colors.textMuted)
        content()
    }
}

/** The text field itself, with [placeholder] showing while [query] is empty. */
@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onSearch: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
) {
    val style = KimonoTheme.typography.body
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.semantics { contentDescription = "Search apps" },
        textStyle = style.copy(color = KimonoTheme.colors.text),
        singleLine = true,
        cursorBrush = SolidColor(KimonoTheme.colors.accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        interactionSource = interactionSource,
        decorationBox = { field ->
            Box {
                // The box always has a fill, so the hint uses the muted colour tuned for one.
                if (query.isEmpty()) Text(placeholder, style = style, color = KimonoTheme.colors.textMutedFocused)
                field()
            }
        },
    )
}

private fun placeholder(lineFocused: Boolean, typing: Boolean): String = when {
    typing -> "type an app name"
    lineFocused -> "press ok to type"
    else -> "search apps"
}

// The field's fill while the line doesn't have focus: there, but quieter than the tile's focused rows.
private const val RESTING_FILL_ALPHA = 0.5f

// The keys a remote or keyboard sends for OK.
private val OkKeys = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)
