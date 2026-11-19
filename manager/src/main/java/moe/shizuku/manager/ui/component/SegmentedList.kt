package moe.shizuku.manager.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import moe.shizuku.manager.ui.miuix.Surface
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.R
import moe.shizuku.manager.ui.theme.LocalAmoledTheme
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.theme.MiuixTheme

// Demo 设置页的卡片：大圆角、纯 surface 白，组内行与行之间没有分割线。
private val SegmentedShape = RoundedCornerShape(16.dp)

/**
 * MIUIX 风格的分段列表：一张 surfaceContainer 卡片，行间用 MIUIX 分割线。
 * 与 Demo 的设置页相同：Card + 分组行 + BasicComponent。
 */
@Composable
fun SegmentedColumn(
    modifier: Modifier = Modifier,
    content: @Composable SegmentedColumnScope.() -> Unit
) {
    // On the pure black theme the card and the page are the same colour, so the card
    // shape disappears and the grouping is lost. A hairline outline in the same tone
    // as the row dividers brings it back without lighting the page up.
    val outlined = LocalAmoledTheme.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (outlined) {
                    Modifier.border(
                        width = 1.dp,
                        color = MiuixTheme.colorScheme.outline,
                        shape = SegmentedShape
                    )
                } else {
                    Modifier
                }
            ),
        shape = SegmentedShape,
        // Demo：卡片是 surfaceContainer（白），页面底色是 surface（浅灰）。
        color = MiuixTheme.colorScheme.surfaceContainer
    ) {
        val scope = SegmentedColumnScope()
        Column { scope.content() }
    }
}

/**
 * One rounded card around a single row, for lists where every entry is its own card
 * the app list, where a flat run of rows reads as one undifferentiated block.
 */
@Composable
fun SegmentedCard(
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.surfaceContainer,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (LocalAmoledTheme.current) {
                    Modifier.border(
                        width = 1.dp,
                        color = MiuixTheme.colorScheme.outline,
                        shape = SegmentedShape
                    )
                } else {
                    Modifier
                }
            ),
        shape = SegmentedShape,
        color = color
    ) {
        content()
    }
}

class SegmentedColumnScope {
    @Composable
    fun item(content: @Composable () -> Unit) {
        // Demo 的卡片组内没有分割线：行直接落在同一张 surface 卡片上。
        content()
    }
}

@Composable
fun SegmentedListItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    // False for a row that already has its own focus stop - a [modifier] that makes it clickable,
    // or a control in [trailingContent] such as a switch - so a TV does not make the row a second
    // stop that, being the outer one, would also keep the D-pad from reaching the control.
    focusableOnTelevision: Boolean = true,
    enabled: Boolean = true,
    // 保留兼容参数；MIUIX BasicComponent 自己负责 start/end action 的对齐。
    centerSlots: Boolean = false,
    switchState: Boolean? = null,
    switchEnabled: Boolean = true,
    onSwitchChange: ((Boolean) -> Unit)? = null
) {
    val dimmed = if (enabled) modifier else modifier.alpha(0.45f)

    // A row that carries a switch toggles from anywhere on it, which is what a settings row
    // is, and the switch becomes decoration: hidden from accessibility, so the row is read
    // once with its state rather than as a row plus a second control saying the same thing.
    val toggle = if (switchState != null && onSwitchChange != null && switchEnabled) {
        { onSwitchChange(!switchState) }
    } else null
    val rowClick = onClick ?: toggle
    val stateText = if (switchState != null) {
        stringResource(if (switchState) R.string.state_on else R.string.state_off)
    } else null

    // TV 上焦点填充整行；点击交给 BasicComponent，按压反馈由 MIUIX Indication 提供。
    val focusModifier = dimmed
        .televisionFocusShape(null)
        .let { base ->
            if (stateText != null) base.semantics { stateDescription = stateText } else base
        }
        .let { base ->
            if (rowClick == null && focusableOnTelevision) base.televisionFocusableRow() else base
        }

    val resolvedTrailing: (@Composable () -> Unit)? = if (switchState != null) {
        {
            Box(modifier = Modifier.clearAndSetSemantics {}) {
                ExpressiveSwitch(
                    checked = switchState,
                    enabled = switchEnabled,
                    onCheckedChange = null
                )
            }
        }
    } else trailingContent

    // Demo 设置页的每一行都是 BasicComponent（ArrowPreference / SwitchPreference 的基座）。
    BasicComponent(
        modifier = focusModifier,
        startAction = if (leadingContent != null) {
            { leadingContent() }
        } else null,
        endActions = if (resolvedTrailing != null) {
            { resolvedTrailing() }
        } else null,
        onClick = rowClick,
        enabled = enabled,
    ) {
        // MIUIX preference 的排版：标题 body1、摘要 body2 + 次要色，不再沿用 Material 的
        // LocalTextStyle（那是 MD3 的 bodyLarge，字重/行高都和 Demo 不同）。
        CompositionLocalProvider(
            LocalTextStyle provides MiuixTheme.textStyles.body1,
            LocalContentColor provides MiuixTheme.colorScheme.onSurfaceContainer,
        ) {
            headlineContent()
        }
        if (supportingContent != null) {
            CompositionLocalProvider(
                LocalTextStyle provides MiuixTheme.textStyles.body2,
                LocalContentColor provides MiuixTheme.colorScheme.onSurfaceContainerVariant,
            ) {
                supportingContent()
            }
        }
    }
}
