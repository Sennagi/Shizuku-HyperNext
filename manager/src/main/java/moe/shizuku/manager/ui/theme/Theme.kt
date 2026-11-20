package moe.shizuku.manager.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.TextStyles
import top.yukonga.miuix.kmp.anim.folmeSpring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.utils.EnvironmentUtils
import androidx.compose.foundation.LocalIndication
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixIndication
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme

/** Shizuku brand indigo, matching the XML theme's primaryColor. */
private val BrandColor = Color(0xFF3F51B5)

/** The indigo seed used until the user chooses another accent. */
const val DEFAULT_ACCENT_COLOR = "#3F51B5"

/**
 * True while the pure black (AMOLED) scheme is in use. Cards and the page are the
 * same colour in that scheme, so surfaces need something else to stay visible see
 * [moe.shizuku.manager.ui.component.SegmentedColumn].
 */
val LocalAmoledTheme = staticCompositionLocalOf { false }

/**
 * 当前是否深色。深浅色的唯一判定点：用户在应用内强制深/浅色时，直接读
 * `isSystemInDarkTheme()` 的地方不会跟随，症状是「只有那一块颜色不对」。
 * 组合树内一律读这个。
 */
val LocalAppDarkMode = staticCompositionLocalOf { false }

/**
 * True on a television. A TV is driven by a remote's D-pad rather than a finger, so the few
 * places that lay themselves out for a thumb - the floating tab bar above all - read this and
 * do something a remote can reach instead. Everything else is the phone layout as it is.
 */
val LocalTelevision = staticCompositionLocalOf { false }

/** Bumped when a theme preference changes so the theme re-reads the prefs. */
object ThemeState {
    var version by mutableIntStateOf(0)
        private set

    fun refresh() {
        version++
    }
}

/**
 * 把 MaterialTheme 的隐式过渡曲线也换成 MIUIX 的 folmeSpring
 * （damping/response 口径与 MIUIX 组件内部一致，见 miuix MiuixEasing.kt）。
 */
private object MiuixMotionScheme : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = folmeSpring(1f, 0.3f)
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = folmeSpring(1f, 0.18f)
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = folmeSpring(0.95f, 0.45f)
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = folmeSpring(1f, 0.25f)
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = folmeSpring(1f, 0.15f)
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = folmeSpring(0.95f, 0.4f)
}

/**
 * MaterialTheme 在这里只是兼容壳：大量历史代码仍写 MaterialTheme.colorScheme.xxx，
 * 但色值全部来自 MIUIX 色板，不再调用 Material 的 dynamicColorScheme/material-kolor。
 * 这样任何残留的 Material 颜色引用显示的也是 MIUIX 风格颜色。
 */
/** MaterialTheme 的字阶映射到 MIUIX textStyles，旧代码的 typography 也跟着变。 */
private fun miuixTypography(t: TextStyles): Typography = Typography(
    displayLarge = t.headline1,
    displayMedium = t.headline2,
    displaySmall = t.title1,
    headlineLarge = t.title1,
    headlineMedium = t.title2,
    headlineSmall = t.title3,
    titleLarge = t.title2,
    titleMedium = t.title3,
    titleSmall = t.title4,
    bodyLarge = t.body1,
    bodyMedium = t.body2,
    bodySmall = t.footnote1,
    labelLarge = t.button,
    labelMedium = t.footnote2,
    labelSmall = t.footnote2,
)

/** MIUIX 的圆角尺度：卡片 16dp、分组件 12dp、小控件 8dp。 */
private fun miuixShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private fun miuixMaterialScheme(c: Colors): ColorScheme = ColorScheme(
    primary = c.primary,
    onPrimary = c.onPrimary,
    primaryContainer = c.primaryContainer,
    onPrimaryContainer = c.onPrimaryContainer,
    inversePrimary = c.onPrimaryVariant,
    secondary = c.secondary,
    onSecondary = c.onSecondary,
    secondaryContainer = c.secondaryContainer,
    onSecondaryContainer = c.onSecondaryContainer,
    tertiary = c.primaryVariant,
    onTertiary = c.onPrimary,
    tertiaryContainer = c.tertiaryContainer,
    onTertiaryContainer = c.onTertiaryContainer,
    background = c.background,
    onBackground = c.onBackground,
    surface = c.surface,
    onSurface = c.onSurface,
    surfaceVariant = c.surfaceVariant,
    onSurfaceVariant = c.onSurfaceVariantSummary,
    surfaceTint = c.primary,
    inverseSurface = c.onSurface,
    inverseOnSurface = c.surface,
    error = c.error,
    onError = c.onError,
    errorContainer = c.errorContainer,
    onErrorContainer = c.onErrorContainer,
    outline = c.outline,
    outlineVariant = c.dividerLine,
    scrim = c.windowDimming,
    surfaceDim = c.surfaceContainerHigh,
    surfaceBright = c.surfaceContainer,
    surfaceContainerLowest = c.background,
    surfaceContainerLow = c.surface,
    surfaceContainer = c.surfaceContainer,
    surfaceContainerHigh = c.surfaceContainerHigh,
    surfaceContainerHighest = c.surfaceContainerHighest,
)

@Composable
fun ShizukuTheme(content: @Composable () -> Unit) {
    // Read the counter so theme changes recompose the tree.
    ThemeState.version

    val context = LocalContext.current
    val prefs = ShizukuSettings.getPreferences()

    val darkTheme = when (ShizukuSettings.getNightMode()) {
        AppCompatDelegate.MODE_NIGHT_NO -> false
        AppCompatDelegate.MODE_NIGHT_YES -> true
        else -> isSystemInDarkTheme()
    }

    // 系统取色只取一个强调色种子，再喂给 MIUIX 色板；不再用 Material 动态调色板。
    val useSystemColor = prefs.getBoolean(ShizukuSettings.Keys.KEY_USE_SYSTEM_COLOR, true) &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val amoled = darkTheme && prefs.getBoolean(ShizukuSettings.Keys.KEY_BLACK_NIGHT_THEME, false)
    val amoledBackgroundOnly = amoled &&
        prefs.getBoolean(ShizukuSettings.Keys.KEY_AMOLED_BACKGROUND_ONLY, false)
    val highContrast = prefs.getBoolean(ShizukuSettings.Keys.KEY_HIGH_CONTRAST, false)
    val accentHex = prefs.getString(
        ShizukuSettings.Keys.KEY_ACCENT_COLOR,
        DEFAULT_ACCENT_COLOR
    ) ?: DEFAULT_ACCENT_COLOR
    val accentColor = remember(accentHex) { parseAccentColor(accentHex) }

    val seedColor = remember(accentColor, useSystemColor, context) {
        if (useSystemColor) {
            runCatching { Color(context.getColor(android.R.color.system_accent1_500)) }
                .getOrDefault(accentColor)
        } else {
            accentColor
        }
    }

    // 一次性把强调色、AMOLED、高对比度覆盖进 MIUIX 自己的色板，
    // 而不是事后 copy（MIUIX 的 Colors 属性是 internal set，重建会丢掉强调色）。
    val miuixDefaults = if (darkTheme) miuixDarkColorScheme() else miuixLightColorScheme()
    val foreground = if (darkTheme) Color.White else Color.Black
    val miuixColors = if (darkTheme) {
        miuixDarkColorScheme(
            primary = seedColor,
            primaryVariant = seedColor,
            background = if (amoled || amoledBackgroundOnly) Color.Black else miuixDefaults.background,
            surface = if (amoled) Color.Black else miuixDefaults.surface,
            surfaceContainer = if (amoled) Color.Black else miuixDefaults.surfaceContainer,
            surfaceContainerHigh = if (amoled) Color.Black else miuixDefaults.surfaceContainerHigh,
            surfaceContainerHighest = if (amoled) Color.Black else miuixDefaults.surfaceContainerHighest,
            onBackground = if (highContrast) foreground else miuixDefaults.onBackground,
            onSurface = if (highContrast) foreground else miuixDefaults.onSurface,
            onSurfaceContainer = if (highContrast) foreground else miuixDefaults.onSurfaceContainer,
            outline = if (highContrast) foreground else miuixDefaults.outline,
            dividerLine = if (highContrast) foreground else miuixDefaults.dividerLine,
        )
    } else {
        miuixLightColorScheme(
            primary = seedColor,
            primaryVariant = seedColor,
            background = miuixDefaults.background,
            onBackground = if (highContrast) foreground else miuixDefaults.onBackground,
            onSurface = if (highContrast) foreground else miuixDefaults.onSurface,
            onSurfaceContainer = if (highContrast) foreground else miuixDefaults.onSurfaceContainer,
            outline = if (highContrast) foreground else miuixDefaults.outline,
            dividerLine = if (highContrast) foreground else miuixDefaults.dividerLine,
        )
    }

    // Match the status/navigation bar icons to the app's theme, not the system's;
    // otherwise a white in-app theme gets light icons on a white bar (invisible).
    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(darkTheme) {
            val window = view.context.findActivity()?.window ?: return@LaunchedEffect
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val television = remember { EnvironmentUtils.isTelevision() }

    CompositionLocalProvider(
        LocalAmoledTheme provides amoled,
        LocalAppDarkMode provides darkTheme,
        LocalTelevision provides television,
    ) {
        MiuixTheme(colors = miuixColors) {
            MaterialTheme(
                colorScheme = miuixMaterialScheme(miuixColors),
                // 字阶和圆角也交给 MIUIX：所有仍写 MaterialTheme.typography 的旧代码
                // 显示的是 MIUIX 字号/行高，而不是 MD3 的 bodyLarge 等。
                typography = miuixTypography(MiuixTheme.textStyles),
                shapes = miuixShapes(),
                motionScheme = MiuixMotionScheme,
            ) {
                // MaterialTheme 会把 LocalIndication 覆盖成 Material 波纹，
                // 这里重新提供 MIUIX 按压反馈（MGAide 同款坑）。
                CompositionLocalProvider(
                    LocalIndication provides MiuixIndication(miuixColors.onSurface)
                ) {
                    content()
                }
            }
        }
    }
}

/** The stored #RRGGBB accent, or the brand colour when it cannot be read. */
private fun parseAccentColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(BrandColor)

private fun Context.findActivity(): Activity? {
    var context: Context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return context as? Activity
}
