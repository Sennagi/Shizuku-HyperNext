package moe.shizuku.manager.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * 状态语义色 token（对齐 Mishka 的 StatusColors）。
 *
 * 调用方一律走 token，不要在屏幕里散落 `Color(0xFF...)`：运行/停止这类语义色
 * 要同时在深色与浅色下成立，散写会漏掉其中一套。
 */
object StatusColors {

    /** 中性灰：未知 / 未测 */
    val neutral: Color
        @Composable @ReadOnlyComposable
        get() = if (LocalAppDarkMode.current) Gray400Dark else Gray500Light

    /** 红：失败 / 停止 / 危险 */
    val danger: Color
        @Composable @ReadOnlyComposable
        get() = if (LocalAppDarkMode.current) Red300Dark else Red600Light

    /** 黄：进行中 / 警告 */
    val warning: Color
        @Composable @ReadOnlyComposable
        get() = if (LocalAppDarkMode.current) Amber300Dark else Amber700Light

    /** 绿：运行中 / 健康 */
    val healthy: Color
        @Composable @ReadOnlyComposable
        get() = if (LocalAppDarkMode.current) Green300Dark else Green600Light

    /** 运行状态前景色。Running 用固定绿；Pending/Stopped 用固定警示色。 */
    @Composable
    @ReadOnlyComposable
    fun runState(state: RunState): Color = when (state) {
        RunState.Running -> healthy
        RunState.Pending -> warning
        RunState.Stopped -> danger
    }

    /**
     * 运行状态卡片背景色。
     *
     * Running 用固定淡绿而不是 secondaryContainer：本项目没有 Monet，主题的
     * primary/secondaryContainer 是随强调色走的灰蓝，套到运行态上整张卡是灰的，
     * 和「正在运行」的语义对不上。Mishka 也只在 Monet 开启时才跟随主题色。
     */
    @Composable
    @ReadOnlyComposable
    fun runStateContainer(state: RunState): Color {
        val isDark = LocalAppDarkMode.current
        return when (state) {
            RunState.Running -> if (isDark) Color(0xFF1A3825) else Color(0xFFDFFAE4)
            RunState.Pending -> if (isDark) Color(0xFF3A3420) else Color(0xFFFFF8E1)
            RunState.Stopped -> if (isDark) Color(0xFF3A2020) else Color(0xFFFDE8E8)
        }
    }

    /**
     * 停止按钮的容器与文字色，对齐 Mishka 的 actionButton(Stop)。
     *
     * 只做停止这一种：Mishka 的 actionButton 覆盖的是 restart/stop/reload 三个
     * 次级动作，而「启动」走的是 textButtonColorsPrimary()（主题主色）。
     * 把启动也做成绿色是错的 —— 那是 restart 的配色。
     */
    @Composable
    @ReadOnlyComposable
    fun stopActionButton(): ActionPalette {
        val isDark = LocalAppDarkMode.current
        return ActionPalette(
            container = if (isDark) Color(0xFF3A1B1B) else Color(0xFFFDE8E8),
            content = if (isDark) Color(0xFFEF5350) else Color(0xFFE53935),
        )
    }

    // Palette: hand-picked values; the scale names are labels, not Material scale steps.
    private val Green300Dark = Color(0xFF81C784)
    private val Green600Light = Color(0xFF4CAF50)
    private val Amber300Dark = Color(0xFFF9A825)
    private val Amber700Light = Color(0xFFFFB300)
    private val Red300Dark = Color(0xFFEF9A9A)
    private val Red600Light = Color(0xFFE53935)
    private val Gray400Dark = Color(0xFFBDBDBD)
    private val Gray500Light = Color(0xFF9E9E9E)
}

/** 运行状态：运行 / 过渡 / 停止 */
enum class RunState { Running, Pending, Stopped }

/** 一组动作按钮配色。 */
data class ActionPalette(val container: Color, val content: Color)
