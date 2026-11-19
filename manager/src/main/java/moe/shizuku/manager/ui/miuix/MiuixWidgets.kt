package moe.shizuku.manager.ui.miuix

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.absoluteValue
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox as MiuixCheckbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator as MiuixCircularProgressIndicator
import top.yukonga.miuix.kmp.basic.FloatingActionButton as MiuixFloatingActionButton
import top.yukonga.miuix.kmp.basic.HorizontalDivider as MiuixHorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator as MiuixInfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator as MiuixLinearProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.RadioButton as MiuixRadioButton
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.glass.GlassTopAppBar
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.glass.GlassIconDropdownMenu
import top.yukonga.miuix.kmp.glass.glassPopupAnchor
import top.yukonga.miuix.kmp.glass.glassPopupAnchorContent
import top.yukonga.miuix.kmp.glass.rememberGlassPopupAnchor

/**
 * Shizuku-Next 的控件统一入口：函数签名沿用原 Material3 调用方式，
 * 实现全部换成 MIUIX 组件。页面只需把 import 从 androidx.compose.material3 换到本包。
 */

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    MiuixText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout ?: {},
        style = style,
    )
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    MiuixText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout ?: {},
        style = style,
    )
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    MiuixIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = if (tint == Color.Unspecified) MiuixTheme.colorScheme.onSurface else tint,
    )
}

@Composable
fun Icon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    MiuixIcon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = if (tint == Color.Unspecified) MiuixTheme.colorScheme.onSurface else tint,
    )
}

@Composable
fun Icon(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    MiuixIcon(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = if (tint == Color.Unspecified) MiuixTheme.colorScheme.onSurface else tint,
    )
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    shape: Any? = null,
    interactionSource: Any? = null,
    content: @Composable () -> Unit,
) {
    MiuixIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content,
    )
}

@Composable
fun FilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    shape: Any? = null,
    interactionSource: Any? = null,
    content: @Composable () -> Unit,
) = IconButton(onClick, modifier, enabled, colors, shape, interactionSource, content)

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: Any? = null,
    contentPadding: Any? = null,
    interactionSource: Any? = null,
    content: @Composable RowScope.() -> Unit,
) {
    MiuixButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = MiuixButtonDefaults.buttonColorsPrimary(),
        content = content,
    )
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: Any? = null,
    contentPadding: Any? = null,
    interactionSource: Any? = null,
    content: @Composable RowScope.() -> Unit,
) = Button(onClick, modifier, enabled, shape, colors, elevation, border, contentPadding, interactionSource, content)

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: Any? = null,
    contentPadding: Any? = null,
    interactionSource: Any? = null,
    content: @Composable RowScope.() -> Unit,
) = Button(onClick, modifier, enabled, shape, colors, elevation, border, contentPadding, interactionSource, content)

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: Any? = null,
    elevation: Any? = null,
    border: Any? = null,
    contentPadding: Any? = null,
    interactionSource: Any? = null,
    content: @Composable RowScope.() -> Unit,
) = Button(onClick, modifier, enabled, shape, colors, elevation, border, contentPadding, interactionSource, content)

@Composable
fun Surface(
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.surface,
    contentColor: Color = MiuixTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    shape: Shape = MiuixSurfaceDefaultsShape,
    border: Any? = null,
    content: @Composable () -> Unit,
) {
    MiuixSurface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        content = content,
    )
}

@Composable
fun Surface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MiuixSurfaceDefaultsShape,
    color: Color = MiuixTheme.colorScheme.surface,
    contentColor: Color = MiuixTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: Any? = null,
    interactionSource: Any? = null,
    content: @Composable () -> Unit,
) {
    MiuixSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = color,
        contentColor = contentColor,
        content = content,
    )
}

private val MiuixSurfaceDefaultsShape: Shape
    @Composable get() = androidx.compose.ui.graphics.RectangleShape

@Composable
fun Card(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MiuixSurfaceDefaultsShape,
    colors: Any? = null,
    elevation: Any? = null,
    border: Any? = null,
    interactionSource: Any? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        MiuixSurface(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            content = content,
        )
    } else {
        MiuixSurface(modifier = modifier, shape = shape, content = content)
    }
}

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: Any? = null,
    interactionSource: Any? = null,
) {
    MiuixSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    interactionSource: Any? = null,
) {
    MiuixCheckbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = onCheckedChange?.let { change -> { change(!checked) } },
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    interactionSource: Any? = null,
) {
    MiuixRadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun LinearProgressIndicator(
    progress: Any?,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
    strokeCap: Any? = null,
) {
    val value = when (progress) {
        is Number -> progress.toFloat()
        is Function0<*> -> (progress as () -> Float).invoke()
        else -> null
    }
    MiuixLinearProgressIndicator(modifier = modifier, progress = value)
}

@Composable
fun LinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
    strokeCap: Any? = null,
) {
    MiuixLinearProgressIndicator(modifier = modifier, progress = null)
}

@Composable
fun CircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = Color.Unspecified,
    strokeCap: Any? = null,
) {
    MiuixCircularProgressIndicator(modifier = modifier, progress = progress)
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = Color.Unspecified,
    strokeCap: Any? = null,
) {
    MiuixInfiniteProgressIndicator(modifier = modifier)
}

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.outline,
    thickness: Dp = 1.dp,
) {
    MiuixHorizontalDivider(modifier = modifier, color = color, thickness = thickness)
}

@Composable
fun SmallFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    containerColor: Color = MiuixTheme.colorScheme.primary,
    contentColor: Color = MiuixTheme.colorScheme.onPrimary,
    elevation: Any? = null,
    content: @Composable () -> Unit,
) {
    MiuixFloatingActionButton(
        onClick = onClick,
        modifier = modifier.widthIn(min = 40.dp),
        shape = shape,
        containerColor = containerColor,
        content = content,
    )
}

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    containerColor: Color = MiuixTheme.colorScheme.primary,
    contentColor: Color = MiuixTheme.colorScheme.onPrimary,
    elevation: Any? = null,
    content: @Composable () -> Unit,
) {
    MiuixFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        content = content,
    )
}

/**
 * 页面级可折叠顶栏的滚动源。Screen 里的 TopAppBar 通过它拿到 ScrollBehavior，
 * 内容根通过 [CollapsibleTopAppBarHost] 的 nestedScroll 把滚动传给同一个实例。
 */
val LocalTopAppBarScrollBehavior = staticCompositionLocalOf<ScrollBehavior?> { null }

/**
 * 页面顶栏的实测高度。MIUIX 顶栏自己吃掉状态栏 inset，展开/折叠时高度会变；
 * 列表首项让开的高度必须等于这个实测值（等价于 Scaffold 的 topBar innerPadding），
 * 不能再手算、也不能额外叠加 statusBar —— 那正是折叠后留白偏大的根因。
 * 由 [TopAppBar] 用 onGloballyPositioned 写入，[topAppBarContentTopPadding] 读取。
 */
val LocalTopBarHeight = staticCompositionLocalOf<MutableState<Dp>?> { null }

/**
 * Demo 的 LocalTopAppBarBackdrop：页面内容记录到图层，玻璃顶栏从中采样做渐进模糊。
 * 只有内容层（顶栏的兄弟节点）能应用 [LocalContentBackdrop]，顶栏本身绝不能在同一图层内，
 * 否则 GlassTopAppBar 内部再建图层会造成父子自引用，RenderNode 递归爆栈。
 */
val LocalTopAppBarBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * 页面内容容器要加的 modifier：`Modifier.weight(1f).then(LocalContentBackdrop.current)`。
 * 与顶栏互为兄弟，所以玻璃顶栏采样它不会递归。
 */
val LocalContentBackdrop = staticCompositionLocalOf<Modifier> { Modifier }

/**
 * 玻璃下拉菜单的页面级宿主。`GlassIconDropdownMenu` 是 BoxScope 扩展，靠
 * `matchParentSize` 覆盖整页定位与拦截点击，所以必须挂在**与锚点同一棵子树的根 Box**
 * 里 —— 顶栏 actions 里那个 48dp 的按钮 Box 装不下它。每个 [IconDropdownMenu]
 * 把自己的弹层注册进宿主，[CollapsibleTopAppBarHost] 的根 Box 负责渲染。
 */
val LocalGlassDropdownHost = staticCompositionLocalOf<GlassDropdownHost?> { null }

/** 一个页面上所有玻璃下拉弹层的注册表。 */
class GlassDropdownHost {
    val slots = mutableStateListOf<@Composable BoxScope.() -> Unit>()
}

/**
 * 顶栏叠加在内容上时，列表首项要让开的高度 = 顶栏实测高度。
 *
 * MIUIX 顶栏默认吃掉状态栏 inset，展开/折叠时自身高度会变；这里直接读 [TopAppBar]
 * 用 onGloballyPositioned 报告的实测值，与官方 Scaffold 用 topBar 高度做 innerPadding
 * 完全等价，且不再重复叠加 statusBar（那正是折叠后列表留白偏大的根因）。
 * 该值同时用于下拉刷新 header 的定位，让刷新头贴着列表第一项、不压住大字标题。
 */
@Composable
fun topAppBarContentTopPadding(): Dp {
    val measured = LocalTopBarHeight.current?.value
    return if (measured != null && measured > 0.dp) {
        measured
    } else {
        // 首帧顶栏还没测量出来前的兜底：状态栏 + 折叠高度。
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 52.dp
    }
}

/**
 * 页面级滚动宿主：nestedScroll 挂在内容根，顶栏读取同一个 ScrollBehavior 折叠。
 * 不再创建 LayerBackdrop —— 0.9.5 的 GlassTopAppBar 采样在部分设备上会渲染错乱。
 */
@Composable
fun CollapsibleTopAppBarHost(
    modifier: Modifier = Modifier,
    blur: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val surfaceColor = MiuixTheme.colorScheme.surface
    // Demo 的 rememberBlurBackdrop：先在图层里铺 surface，再记录内容。
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    // 玻璃下拉弹层的宿主：与页面内容同一个根 Box（GlassIconDropdownMenu 的要求）。
    val dropdownHost = remember { GlassDropdownHost() }
    // 顶栏实测高度：TopAppBar 写入，列表 padding 读取。
    val topBarHeight = remember { mutableStateOf(0.dp) }
    CompositionLocalProvider(
        LocalTopAppBarScrollBehavior provides scrollBehavior,
        LocalTopAppBarBackdrop provides if (blur) backdrop else null,
        // 内容容器（铺满整页、顶栏叠加在它上面）负责记录，顶栏才能采样到它下面的内容。
        LocalContentBackdrop provides if (blur) Modifier.layerBackdrop(backdrop) else Modifier,
        LocalGlassDropdownHost provides dropdownHost,
        LocalTopBarHeight provides topBarHeight,
    ) {
        Box(modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
            content()
            // 玻璃下拉弹层最后渲染：铺满整页的定位层 + 展开面板都在这里，
            // 不受顶栏 48dp 按钮容器的尺寸与裁剪影响。
            dropdownHost.slots.forEach { it() }
        }
    }
}

@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: Any? = null,
    colors: Any? = null,
    scrollBehavior: Any? = null,
    titleContentColor: Any? = null,
    actionIconContentColor: Any? = null,
    navigationIconContentColor: Any? = null,
    maxLines: Any? = null,
    subtitle: String = "",
) {
    val effectiveScrollBehavior =
        scrollBehavior as? ScrollBehavior ?: LocalTopAppBarScrollBehavior.current
    val backdrop = LocalTopAppBarBackdrop.current
    val surfaceColor = MiuixTheme.colorScheme.surface
    // 实测顶栏高度（含状态栏 inset、随展开/折叠变化），供列表 padding 跟随。
    val localTopBarHeight = LocalTopBarHeight.current
    val measuredModifier = modifier.onGloballyPositioned { coords ->
        val dp = with(coords.density) { coords.size.height.toDp() }
        if (localTopBarHeight != null && (localTopBarHeight.value - dp).absoluteValue > 0.5.dp) {
            localTopBarHeight.value = dp
        }
    }
    if (backdrop != null && isRuntimeShaderSupported()) {
        // 与 Demo 一致：顶栏透明，滚动时从内容图层采样做渐进式模糊。
        // 前提是内容铺满整页、顶栏叠加在上方（见 CollapsibleTopAppBarHost 的注释）。
        GlassTopAppBar(
            title = title,
            modifier = measuredModifier,
            subtitle = subtitle,
            scrollBehavior = effectiveScrollBehavior,
            backdrop = backdrop,
            progressiveBlur = true,
            bandBrush = GlassTopAppBarDefaults.bandBrush(surfaceColor),
            bandOverhang = GlassTopAppBarDefaults.BandOverhang,
            navigationIcon = navigationIcon,
            actions = actions,
        )
    } else {
        MiuixTopAppBar(
            title = title,
            modifier = measuredModifier,
            navigationIcon = navigationIcon,
            actions = actions,
            subtitle = subtitle,
            scrollBehavior = effectiveScrollBehavior,
        )
    }
}

/**
 * 少数页面的标题不是纯文本（带副标题、搜索框），保留原调用签名，
 * 但依然用 MIUIX TopAppBar 渲染：把自定义标题放进 navigationIcon 槽位。
 */
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: Any? = null,
    colors: Any? = null,
    scrollBehavior: Any? = null,
    titleContentColor: Any? = null,
    actionIconContentColor: Any? = null,
    navigationIconContentColor: Any? = null,
    maxLines: Any? = null,
) {
    val effectiveScrollBehavior =
        scrollBehavior as? ScrollBehavior ?: LocalTopAppBarScrollBehavior.current
    val customTitle: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            navigationIcon()
            title()
        }
    }
    val backdrop = LocalTopAppBarBackdrop.current
    val localTopBarHeight = LocalTopBarHeight.current
    val measuredModifier = modifier.onGloballyPositioned { coords ->
        val dp = with(coords.density) { coords.size.height.toDp() }
        if (localTopBarHeight != null && (localTopBarHeight.value - dp).absoluteValue > 0.5.dp) {
            localTopBarHeight.value = dp
        }
    }
    if (backdrop != null && isRuntimeShaderSupported()) {
        GlassTopAppBar(
            title = "",
            modifier = measuredModifier,
            scrollBehavior = effectiveScrollBehavior,
            backdrop = backdrop,
            progressiveBlur = true,
            bandBrush = GlassTopAppBarDefaults.bandBrush(MiuixTheme.colorScheme.surface),
            bandOverhang = GlassTopAppBarDefaults.BandOverhang,
            navigationIcon = customTitle,
            actions = actions,
        )
    } else {
        MiuixTopAppBar(
            title = "",
            modifier = measuredModifier,
            navigationIcon = customTitle,
            actions = actions,
            scrollBehavior = effectiveScrollBehavior,
        )
    }
}

/**
 * 对话框底座的换法：MIUIX 0.9.5 的 [WindowDialog] 在自己的窗口里调
 * `WindowNavigationEventScope`，它从 `LocalView` 找 `NavigationEventDispatcherOwner`；
 * 弹窗的 view 不在 Activity 的 decorView 之下，于是拿到 null，
 * 内部的后退处理随即抛
 * `IllegalStateException: No NavigationEventDispatcher was provided via LocalNavigationEventDispatcherOwner`
 * ——点开调参/安装对话框就闪退正是这个。换成 Compose 自己的 [Dialog]：
 * 它用父 Composition 建子组合，locals 全继承（含 owner），MIUIX 的 window 组件不再参与。
 */
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape? = null,
    containerColor: Color = DialogDefaults.backgroundColor(),
    tonalElevation: Dp = 0.dp,
    iconContentColor: Any? = null,
    titleContentColor: Any? = null,
    textContentColor: Any? = null,
    properties: Any? = null,
) {
    // Demo 的弹窗是「略小 + 透明」淡入到原尺寸的；Compose 的 Dialog 本身没有这段，
    // 不补就会出现"啪"地一下蹦出来。
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val enter by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "miuix-dialog",
    )
    Dialog(onDismissRequest = onDismissRequest) {
        MiuixSurface(
            modifier = modifier.graphicsLayer {
                alpha = enter
                val scale = 0.92f + 0.08f * enter
                scaleX = scale
                scaleY = scale
            },
            shape = shape ?: RoundedCornerShape(24.dp),
            color = containerColor,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                icon?.invoke()
                if (title != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MiuixTheme.textStyles.title3,
                        LocalContentColor provides MiuixTheme.colorScheme.onSurfaceContainer,
                    ) {
                        title.invoke()
                    }
                }
                if (text != null) {
                    Box(modifier = Modifier.padding(top = if (title != null) 10.dp else 0.dp)) {
                        CompositionLocalProvider(
                            LocalTextStyle provides MiuixTheme.textStyles.body2,
                            LocalContentColor provides MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        ) {
                            text.invoke()
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

/**
 * 顶栏图标下拉菜单 —— 0.9.5-beta01 官方的那条链路，一比一：
 *
 * - `rememberGlassPopupAnchor()` 造锚点，`Modifier.glassPopupAnchor(anchor, cornerRadius)`
 *   挂在按钮最外层：上报按钮的窗口矩形，弹窗激活期间自动把按钮本体淡出。
 * - [GlassIconDropdownMenu]（官方 Demo 顶栏右上角那个）负责面板与全部过渡动画：
 *   按钮那块玻璃形变成展开面板（GlassMotion.transformBounds），材质交叉渐变，
 *   全部是库自带，不再自己画任何东西。
 * - 它是 BoxScope 扩展，靠 matchParentSize 覆盖整页，不能放进顶栏那个 48dp 的按钮
 *   Box —— 所以通过 [LocalGlassDropdownHost] 注册到 [CollapsibleTopAppBarHost] 的
 *   页面根 Box 里渲染（官方 KDoc 要求与锚点在同一根 BoxScope）。
 */
@Composable
fun IconDropdownMenu(
    entry: DropdownEntry,
    summary: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val anchor = rememberGlassPopupAnchor()
    val currentEntry by rememberUpdatedState(entry)
    val currentContent by rememberUpdatedState(content)
    val host = LocalGlassDropdownHost.current
    // GlassTransformPopup 必须采样一张**真实录制过内容**的 Backdrop 才能画出玻璃面板：
    // 空图层的采样结果什么都没有，表现为「按钮被淡出但面板不出现」。
    // 本页开了 blur 就有内容层 backdrop，否则退回空图层（面板会退化为纯材质色）。
    val backdrop = LocalTopAppBarBackdrop.current ?: rememberLayerBackdrop()

    if (host != null) {
        DisposableEffect(host, anchor) {
            val slot: @Composable BoxScope.() -> Unit = {
                GlassIconDropdownMenu(
                    show = expanded,
                    onDismissRequest = { expanded = false },
                    anchor = anchor,
                    anchorContent = { currentContent() },
                    entries = listOf(currentEntry),
                    backdrop = backdrop,
                )
            }
            host.slots.add(slot)
            onDispose { host.slots.remove(slot) }
        }
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .glassPopupAnchor(anchor, cornerRadius = 24.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled) { expanded = true },
        contentAlignment = Alignment.Center,
    ) {
        // 告诉库「图标」的实际边界：形变动画复制这块内容、按它的中心位移，
        // 不写这个 modifier 时库会复制整个 48dp 控件，图标就会飘出去再缩回（位移）。
        Box(modifier = Modifier.glassPopupAnchorContent(anchor)) {
            content()
        }
    }
}
@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    overlineContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    verticalAlignment: Any? = null,
    selected: Boolean = false,
    headlineContent: @Composable () -> Unit = {},
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    val content: @Composable () -> Unit = {
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (leadingContent != null) leadingContent()
            Column(modifier = Modifier.weight(1f)) {
                overlineContent?.invoke()
                headlineContent()
                supportingContent?.invoke()
            }
            if (trailingContent != null) trailingContent()
        }
    }
    val surfaceColor = MiuixTheme.colorScheme.surfaceContainer
    if (onClick != null) {
        MiuixSurface(onClick = onClick, color = surfaceColor, enabled = enabled, content = content)
    } else {
        MiuixSurface(color = surfaceColor, content = content)
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable () -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    colors: Any? = null,
    border: Any? = null,
    shape: Shape? = null,
    interactionSource: Any? = null,
) {
    MiuixSurface(onClick = onClick, modifier = modifier.heightIn(min = 32.dp), enabled = enabled) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) leadingIcon()
            label()
            if (trailingIcon != null) trailingIcon()
        }
    }
}

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: Any? = null,
    shape: Shape? = null,
    colors: Any? = null,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    MiuixTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        textStyle = textStyle,
    )
}

@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: Any? = null,
    shape: Shape? = null,
    colors: Any? = null,
    textStyle: TextStyle = LocalTextStyle.current,
) = TextField(
    value, onValueChange, modifier, enabled, readOnly, label, placeholder, leadingIcon, trailingIcon,
    isError, visualTransformation, keyboardOptions, keyboardActions, singleLine, maxLines, minLines,
    interactionSource, shape, colors, textStyle,
)

@Composable
fun TextField(
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: Any? = null,
    shape: Shape? = null,
    colors: Any? = null,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    MiuixTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        textStyle = textStyle,
    )
}

@Composable
fun OutlinedTextField(
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: Any? = null,
    shape: Shape? = null,
    colors: Any? = null,
    textStyle: TextStyle = LocalTextStyle.current,
) = TextField(
    value, onValueChange, modifier, enabled, readOnly, label, placeholder, leadingIcon, trailingIcon,
    isError, visualTransformation, keyboardOptions, keyboardActions, singleLine, maxLines, minLines,
    interactionSource, shape, colors, textStyle,
)

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    MiuixInfiniteProgressIndicator(modifier = modifier)
}

@Composable
fun LinearWavyProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Any? = null,
    color: Color = Color.Unspecified,
) {
    val value = when (progress) {
        is Number -> progress.toFloat()
        is Function0<*> -> (progress as () -> Float).invoke()
        else -> null
    }
    MiuixLinearProgressIndicator(modifier = modifier, progress = value)
}

@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: Any? = null,
    properties: Any? = null,
    dragHandle: Any? = null,
    containerColor: Any? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    OverlayBottomSheet(
        show = true,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Column(content = content)
    }
}

@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: Any? = null,
    properties: Any? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return
    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        MiuixSurface(
            modifier = modifier.padding(vertical = 4.dp).width(IntrinsicSize.Min),
            color = MiuixTheme.colorScheme.surfaceContainer,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            shadowElevation = 8.dp,
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: Any? = null,
    contentDescription: Any? = null,
) {
    MiuixSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 12.dp),
        enabled = enabled,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) leadingIcon()
            Box(modifier = Modifier.weight(1f)) { text() }
            if (trailingIcon != null) trailingIcon()
        }
    }
}

@Composable
fun AssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    colors: Any? = null,
    shape: Shape? = null,
    border: Any? = null,
    interactionSource: Any? = null,
) {
    MiuixSurface(onClick = onClick, modifier = modifier.heightIn(min = 32.dp), enabled = enabled) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) leadingIcon()
            label()
            if (trailingIcon != null) trailingIcon()
        }
    }
}
