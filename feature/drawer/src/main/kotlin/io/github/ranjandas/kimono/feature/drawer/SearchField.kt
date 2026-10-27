package io.github.ranjandas.kimono.feature.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.text.count
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

/**
 * The search line at the top of all apps, as a real text field so the on-screen keyboard and the Google TV phone
 * app's keyboard can type into it. Moving onto it with the D-pad only highlights it; OK brings up the keyboard,
 * and the keyboard's search key ([onDone]) goes back to the list. [onFocusChange] says when it has focus, and
 * [focusRequester] focuses it.
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
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = KimonoTheme.colors
    val style = KimonoTheme.typography.headline
    val focusChange by rememberUpdatedState(onFocusChange)
    LaunchedEffect(focused) { focusChange(focused) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (focused) colors.surfaceVariant else colors.surface, KimonoTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("~/apps ❯", style = style, color = colors.accent)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .semantics { contentDescription = "Search apps" }
                // OK on a remote brings up the keyboard; the field itself only does that for a touch.
                .onPreviewKeyEvent { event ->
                    val ok = event.key == Key.DirectionCenter
                    if (ok && event.type == KeyEventType.KeyUp) keyboard?.show()
                    ok
                },
            textStyle = style.copy(color = colors.text),
            singleLine = true,
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboard?.hide()
                onDone()
            }),
            interactionSource = interactionSource,
            decorationBox = { field ->
                Box {
                    if (query.isEmpty()) {
                        Text(if (focused) "press ok to type" else "search", style = style, color = colors.textMuted)
                    }
                    field()
                }
            },
        )
        Text(count(matches, "app"), style = KimonoTheme.typography.label, color = colors.textMuted)
    }
}
