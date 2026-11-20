package moe.shizuku.manager.ui

import android.app.Activity
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.basic.Badge
import androidx.compose.material3.MaterialTheme
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.glass.GlassNavigationBar
import top.yukonga.miuix.kmp.glass.GlassNavigationBarDefaults
import top.yukonga.miuix.kmp.glass.GlassNavigationItem
import top.yukonga.miuix.kmp.theme.MiuixTheme
import moe.shizuku.manager.ui.miuix.CollapsibleTopAppBarHost
import moe.shizuku.manager.ui.miuix.Surface
import moe.shizuku.manager.ui.miuix.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ui.component.TelevisionFocusOutline
import moe.shizuku.manager.ui.component.televisionFocusShape
import moe.shizuku.manager.ui.screen.AccessibilityScreen
import moe.shizuku.manager.ui.screen.AppsScreen
import moe.shizuku.manager.ui.screen.ActivitiesScreen
import moe.shizuku.manager.ui.screen.FilesScreen
import moe.shizuku.manager.ui.screen.ForceDarkScreen
import moe.shizuku.manager.ui.screen.HidingScreen
import moe.shizuku.manager.ui.screen.HomeScreen
import moe.shizuku.manager.ui.screen.AppToggleFeature
import moe.shizuku.manager.ui.screen.InstallerScreen
import moe.shizuku.manager.ui.screen.PowerScreen
import moe.shizuku.manager.ui.screen.RotationScreen
import moe.shizuku.manager.ui.screen.IntentsScreen
import moe.shizuku.manager.ui.screen.DeviceInfoScreen
import moe.shizuku.manager.ui.screen.DisplayScreen
import moe.shizuku.manager.ui.screen.LabsScreen
import moe.shizuku.manager.ui.screen.LogScreen
import moe.shizuku.manager.ui.screen.LabsToggleScreen
import moe.shizuku.manager.ui.screen.ManageScreen
import moe.shizuku.manager.ui.screen.CatalogKind
import moe.shizuku.manager.ui.screen.CatalogScreen
import moe.shizuku.manager.ui.screen.PermissionsScreen
import moe.shizuku.manager.ui.screen.SettingsScreen
import moe.shizuku.manager.ui.screen.ShellScreen
import moe.shizuku.manager.ui.screen.ModulesScreen
import moe.shizuku.manager.ui.screen.StealthScreen
import moe.shizuku.manager.ui.screen.TerminalScreen
import moe.shizuku.manager.ui.screen.WifiPasswordsScreen
import moe.shizuku.manager.ui.theme.LocalAmoledTheme
import moe.shizuku.manager.ui.theme.LocalTelevision
import moe.shizuku.manager.ui.theme.ShizukuTheme

/**
 * A secondary screen shown on top of the tab pager.
 *
 * The first two are what the Labs tab opens, and they are the reason it exists: an app's ops
 * and a shell are things you go to, not places you live, and giving each of them a tab meant
 * the bar spent its whole width on five icons while the two screens behind two of them were
 * mostly empty when you arrived.
 */
enum class Detail(
    /** The list this screen opens, for the six that are one list with one switch per app. */
    val feature: AppToggleFeature? = null
) {
    APP_OPS,
    SHELL,
    FIREWALL(AppToggleFeature.FIREWALL),
    AUTOSTART(AppToggleFeature.AUTOSTART),

    /**
     * The six hiding modes, as one tile. Each mode still has its own detail below, which is what
     * the list screens are addressed by, but the grid offers this one and the modes are reached
     * through the screen it opens.
     */
    HIDING,
    HIDE_DEVELOPER_OPTIONS(AppToggleFeature.HIDE_DEVELOPER_OPTIONS),
    HIDE_USB_DEBUGGING(AppToggleFeature.HIDE_USB_DEBUGGING),
    HIDE_WIRELESS_DEBUGGING(AppToggleFeature.HIDE_WIRELESS_DEBUGGING),
    HIDE_ACCESSIBILITY(AppToggleFeature.HIDE_ACCESSIBILITY),
    HIDE_PRIVATE_DNS(AppToggleFeature.HIDE_PRIVATE_DNS),
    HIDE_VPN(AppToggleFeature.HIDE_VPN),
    LOG,
    DEVICE_INFO,
    FORCE_DARK,
    ACTIVITIES,

    /** Reboot, recovery, bootloader, power off and the sleep key, through the shell. */
    POWER,

    /** Resolution, density and refresh rate, as the shell changes them. */
    DISPLAY,

    /** Per-app override for apps that force themselves into landscape. */
    ROTATION,

    /** The saved wifi networks and their keys, read as the shell. */
    WIFI_PASSWORDS,

    /** The filesystem, as the shell sees it: Android/data and what an app cannot open itself. */
    FILES,

    /** Installing a package with the flags a terminal would need a cable for. */
    INSTALL,
    STEALTH,
    TERMINAL,
    INTENTS,
    PERMISSIONS,

    /** Every accessibility service, with the switch the system screen owns and a keep-alive pin. */
    ACCESSIBILITY,

    /** The community's module index, shown here only when there was no browser to open it in. */
    MORE_APPS;

    companion object {
        /** The detail that opens [feature]'s list. Every feature has exactly one. */
        fun of(feature: AppToggleFeature): Detail? = entries.firstOrNull { it.feature == feature }
    }
}

/**
 * On wide windows (tablets, foldables, desktop mode, mirrored displays) a
 * single-column layout stretched edge to edge looks sparse, so the content is
 * capped at this width and centred. On a phone the window is narrower than the
 * cap, so this has no effect.
 */
private val MaxContentWidth = 600.dp



private data class Tab(
    val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * The key the tab pages' saveable state is filed under, so it survives while a detail screen is
 * the one on screen. One key rather than one per tab: the pager keeps every page it has already
 * visited, so the pages are all inside this one provider and none of them needs its own.
 */
private const val TAB_PAGES = "tab-pages"

/**
 * The tab bar's pill, and where the tabs it travels between sit on the bar.
 *
 * Both live above the switch that shows a detail screen, because the bar leaves the composition
 * while a detail is open. Measured again from nothing on the way back, the pill began its travel
 * at the bar's own zero - the left end, which is Home - so coming back from Intents the pill walked
 * the width of the bar to Settings before coming to rest on the tab that had never stopped being
 * the selected one. It is the same shape of bug as the pager's, one level down: state the way back
 * needs was remembered by the thing that goes away.
 *
 * The pill is an [Animatable] rather than an `animateFloatAsState` so that the first place it is
 * given can be given to it rather than travelled to. Only a tab actually chosen is a move.
 */
private class TabPill {
    val bounds = mutableStateMapOf<Int, Rect>()
    val left = Animatable(0f)
    val width = Animatable(0f)

    /** False until the pill has been placed once, which is the one move there is nothing to make. */
    var placed = false
}

private val tabs = listOf(
    Tab(R.string.tab_home, MiuixIcons.Home, MiuixIcons.Home),
    // The two icons their own app uses for these two screens: the panel for what an app is
    // allowed to do, the stacked layers for the modules that add to it.
    Tab(R.string.tab_apps, MiuixIcons.Settings, MiuixIcons.Settings),
    Tab(R.string.tab_modules, MiuixIcons.Layers, MiuixIcons.Layers),
    // Labs holds the things that are gone to rather than lived in: the app-ops list and the
    // shell to begin with, and whatever else turns out to belong there. Each was a tab of its
    // own before, which is a lot of the bar for two screens that are mostly a list you read
    // once, and it also made the shell's own session the price of switching to Settings.
    Tab(R.string.tab_labs, MiuixIcons.Help, MiuixIcons.Help),
    Tab(R.string.tab_settings, MiuixIcons.Settings, MiuixIcons.Settings),
)

@Composable
fun ShizukuApp(onRunSetup: () -> Unit) {
    ShizukuTheme {
        var alwaysShowNavigationBar by remember {
            mutableStateOf(ShizukuSettings.getAlwaysShowNavigationBar())
        }
        // Detail screens are shown outside the Scaffold, so wrap everything in a
        // Surface otherwise LocalContentColor falls back to black and plain
        // Text becomes unreadable in dark themes.
        // Demo 的根节点就是 MIUIX 的 Scaffold：Overlay* 系弹层（图标下拉菜单、
        // 底部弹层）全靠它提供的 LocalRootPopupStates 渲染，没有这层，弹层注册进去
        // 也没有宿主。这里 topBar/bottomBar 留空，内容自己处理系统栏 inset。
        Scaffold {
        Surface(
            modifier = Modifier.fillMaxSize(),
            // Demo 的页面底色是 MIUIX surface（浅灰），白色卡片是 surfaceContainer。
            color = MiuixTheme.colorScheme.surface
        ) {
            TelevisionFocusOutline {
            // 状态栏透明 + 内容延伸到系统栏下面（和官方 Demo 一致），
            // 顶部 inset 由 MIUIX 顶栏自己处理，这里不能再加一次 statusBars padding。
            // 必须是 Box 而不是 Column：玻璃底栏是浮在内容之上的叠加层（靠
            // align(BottomCenter) 定位），Column 是纵向线性布局，会把底栏排在内容
            // 下方 —— 内容一长就把底栏挤出屏幕，表现为「滑到最底部才看到底栏渲染错位」。
            Box(modifier = Modifier.fillMaxSize()) {
                // Both the open detail and the pager live here, above the switch between
                // the two, for two reasons: the pager used to be remembered inside the tab
                // layout, so opening a detail threw it away and closing the detail started
                // again on the first tab (back from a Settings screen landed on Home). And
                // neither was saveable, so a rotation dropped both and did the same thing.
                var detail by rememberSaveable { mutableStateOf<Detail?>(null) }
                val pagerState = rememberPagerState(pageCount = { tabs.size })
                val pill = remember { TabPill() }
                // The pages leave the composition while a detail is open, and where a list is
                // scrolled to is saveable state - which is discarded when the composable holding
                // it goes away. So going back from Intents landed at the top of Settings rather
                // than where it was left, and the pager alone could not fix that: this holds the
                // state of the pages while they are gone, which is the same thing a navigation
                // library does, and for the same reason.
                val pages = rememberSaveableStateHolder()
                // The tab a TV is on. Kept up here beside the pager for the same reason the
                // pager is: a detail screen takes the tabs out of the composition, and the way
                // back should land on the tab it left.
                var tvTab by rememberSaveable { mutableIntStateOf(0) }
                // Whether a detail screen has been open, so the TV's tabs coming back after it
                // put the focus on the page it was opened from rather than on the rail.
                var tvDetailWasOpen by remember { mutableStateOf(false) }
                val tvReturn = remember { TelevisionReturn() }
                val current = detail

                // 子页进出用 MIUIX 的 directional 过渡：去子页时新页从右侧滑入、
                // 旧页向左让位并淡出；返回时反过来。整页只做一次转场，内容层自己
                // 记录的 backdrop 仍由各页负责，不会和顶栏互相采样。
                BackHandler(enabled = current != null) { detail = null }
                AnimatedContent(
                    targetState = current,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        val forward = targetState != null
                        val enter = slideInHorizontally(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            initialOffsetX = { if (forward) it / 4 else -it / 6 },
                        ) + fadeIn(tween(durationMillis = 220))
                        val exit = slideOutHorizontally(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            targetOffsetX = { if (forward) -it / 6 else it / 4 },
                        ) + fadeOut(tween(durationMillis = 180))
                        enter togetherWith exit
                    },
                    label = "detail",
                ) { target ->
                    if (target != null) {
                        SideEffect { tvDetailWasOpen = true }
                        CollapsibleTopAppBarHost(modifier = Modifier.fillMaxSize()) {
                    CenteredContent(
                        // A detail screen has no tab bar under it, so it is the one that has
                        // to keep clear of the navigation bar itself. The tab layout adds
                        // that inset to its own bottom padding; a detail had none, which on
                        // a device with navigation buttons put the shell's input row under
                        // them.
                        modifier = Modifier.windowInsetsPadding(
                            WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                        )
                    ) {
                        when (target) {
                            // Both get the bar's padding of zero: a detail has no floating
                            // bar under it to keep clear of, and this wrapper already keeps
                            // them off the navigation bar.
                            Detail.APP_OPS -> ManageScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.SHELL -> ShellScreen(bottomPadding = 0.dp, onBack = { detail = null })
                            // The six hiding modes are one tile in the grid, and this is what
                            // it opens: all of them, what each covers and whether it is on.
                            Detail.HIDING -> HidingScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null },
                                onOpenDetail = { detail = it }
                            )

                            Detail.FIREWALL,
                            Detail.AUTOSTART,
                            Detail.HIDE_DEVELOPER_OPTIONS,
                            Detail.HIDE_USB_DEBUGGING,
                            Detail.HIDE_WIRELESS_DEBUGGING,
                            Detail.HIDE_ACCESSIBILITY,
                            Detail.HIDE_PRIVATE_DNS,
                            Detail.HIDE_VPN -> LabsToggleScreen(
                                // Every one of these is a list with a switch per app, so the
                                // feature comes with the screen rather than beside it.
                                feature = requireNotNull(target.feature),
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.DEVICE_INFO -> DeviceInfoScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.FORCE_DARK -> ForceDarkScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.ACTIVITIES -> ActivitiesScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.WIFI_PASSWORDS -> WifiPasswordsScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.FILES -> FilesScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.INSTALL -> InstallerScreen(
                                path = null,
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )


                            Detail.DISPLAY -> DisplayScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.ROTATION -> RotationScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.POWER -> PowerScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.LOG -> LogScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )

                            Detail.MORE_APPS -> CatalogScreen(
                                kind = CatalogKind.APPS,
                                // The apps list is not modules, so nothing here can be
                                // already installed as one.
                                installedKeys = emptySet(),
                                onBack = { detail = null },
                                onInstalled = {}
                            )

                            Detail.STEALTH -> StealthScreen(onBack = { detail = null })
                            Detail.TERMINAL -> TerminalScreen(onBack = { detail = null })
                            Detail.INTENTS -> IntentsScreen(onBack = { detail = null })
                            Detail.PERMISSIONS -> PermissionsScreen(onBack = { detail = null })
                            Detail.ACCESSIBILITY -> AccessibilityScreen(
                                bottomPadding = 0.dp,
                                onBack = { detail = null }
                            )
                        }
                    }
                    }
                } else if (LocalTelevision.current) {
                    TelevisionTabs(
                        selected = tvTab,
                        onSelect = { tvTab = it },
                        pages = pages,
                        focusPageFirst = tvDetailWasOpen,
                        returnFocus = tvReturn,
                        onOpenDetail = {
                            tvReturn.detail = it
                            detail = it
                        },
                        onRunSetup = onRunSetup
                    )
                } else {
                    pages.SaveableStateProvider(TAB_PAGES) {
                        MainTabs(
                            pagerState = pagerState,
                            pill = pill,
                            alwaysShowNavigationBar = alwaysShowNavigationBar,
                            onAlwaysShowNavigationBarChange = {
                                ShizukuSettings.setAlwaysShowNavigationBar(it)
                                alwaysShowNavigationBar = it
                            },
                            onOpenDetail = { detail = it },
                            onRunSetup = onRunSetup
                        )
                    }
                }
                }
            }
            }
        }
        }
    }
}

@Composable
private fun CenteredContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                // widthIn must come first: fillMaxWidth sets min == max, which
                // would defeat a later widthIn cap.
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            content()
        }
    }
}

internal enum class BackAction { GO_HOME, EXIT, SHOW_HINT }

/**
 * What a back press means, from the tab it was pressed on and the one before it.
 *
 * Back is not a history: it takes any tab to Home, the app's root. From Home, a second press
 * within [windowMs] leaves the app, and a first press explains that. Keeping the decision here
 * rather than in the handler is what lets the test state it without a device.
 */
internal fun backAction(
    page: Int,
    lastBackAt: Long,
    now: Long,
    windowMs: Long = 2_000L
): BackAction = when {
    page != 0 -> BackAction.GO_HOME
    lastBackAt != 0L && now - lastBackAt in 0L..windowMs -> BackAction.EXIT
    else -> BackAction.SHOW_HINT
}

@Composable
private fun MainTabs(
    pagerState: PagerState,
    pill: TabPill,
    alwaysShowNavigationBar: Boolean,
    onAlwaysShowNavigationBarChange: (Boolean) -> Unit,
    onOpenDetail: (Detail) -> Unit,
    onRunSetup: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    // Back is not a tab history. Any tab goes Home without claiming to be the exit press; the
    // hint belongs to Home, where a second press within two seconds leaves. A page's own back -
    // the Activities launcher's app list, a module's full screen, a Settings category - is
    // composed inside this one, so its handler answers first.
    var lastBackAt by remember { mutableLongStateOf(0L) }

    // A move between tabs starts the count again. The Home press that shows the hint changes no
    // page, so its timestamp stays until the next press or a tab change.
    LaunchedEffect(pagerState.settledPage) {
        lastBackAt = 0L
    }

    BackHandler {
        val now = SystemClock.elapsedRealtime()
        when (backAction(pagerState.currentPage, lastBackAt, now)) {
            BackAction.GO_HOME -> {
                lastBackAt = 0L
                scope.launch { pagerState.animateScrollToPage(0) }
            }
            BackAction.EXIT -> activity?.finish()
            BackAction.SHOW_HINT -> {
                lastBackAt = now
                Toast.makeText(context, R.string.toast_press_back_again, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Whether the app has settled enough to read the lists nobody is looking at yet. Both app
    // lists are expensive to read - six hundred packages each - and a pager composes the page
    // being dragged in, so reading on composition spent swipes on them. Reading when the tab
    // is landed on fixed the swipe and made the first visit wait instead, which is what this
    // takes back: shortly after launch, while nobody is touching anything, the two lists read
    // themselves, so the swipe is still cheap and the tab is already full when it is opened.
    var warmUp by remember { mutableStateOf(false) }

    // Whether the module screen has one of its own full screens up - a module's page, its
    // action console or the catalog. Those take the whole tab, and a horizontal swipe there
    // belongs to what is on screen rather than to the app's tabs, so the pager stops dragging
    // while one of them is up.
    var modulesFullscreen by remember { mutableStateOf(false) }
    // 设置子页面是全屏的独立页面，底栏要跟着隐藏。
    var settingsSubPage by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1500)
        warmUp = true
    }

    // The number of apps Shizuku's permission is granted to, as last reported by the Apps page.
    // That page owns the read - it is the one with the list - and this is where its answer is
    // kept so the tab bar can show it without a second pass over the same packages.
    var grantedCount by remember { mutableIntStateOf(0) }

    // GlassNavigationBar 自带底部悬浮边距（28dp 或系统导航栏高度），内容只需再让出胶囊本身。
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val barInset = if (navBarBottom > 28.dp) navBarBottom else 28.dp
    val bottomPadding = GlassNavigationBarDefaults.Height + barInset + 8.dp

    // 官方 Demo 的玻璃底栏靠 layerBackdrop 采样背后的内容。
    // 这里必须和顶栏那份 backdrop 一样，先用 drawRect 铺一层页面底色再 drawContent：
    // 空的 rememberLayerBackdrop() 没有任何绘制指令，玻璃采样到的就是空图层 ——
    // 表现为底栏变成一块不跟随内容变化的纯色（滑到列表底部时最明显）。
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // backdrop 必须记录「整块内容区」的绘制结果：玻璃底栏靠它采样背后有什么。
        // 之前把它挂在 CenteredContent 上（限宽 600dp 的那层），内容滚动到底部时
        // 该层里可采样的像素变少，底栏就退化成一块纯色 —— 表现就是「滑到最底部
        // 玻璃效果没了」。这里改挂在外层 Box，整屏内容都进图层。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
        CenteredContent(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !(pagerState.currentPage == 2 && modulesFullscreen),
                modifier = Modifier.fillMaxSize(),
                // Every page stays composed. The pager otherwise builds a page only as it is
                // dragged in, which is what the app lists were reading on; keeping them all
                // alive is what lets the warm-up below reach them before anyone opens them,
                // and it is also what keeps a tab's contents across a visit - the android
                // tab bar's own pages are cheap to hold, and only the rows on screen are
                // ever built.
                beyondViewportPageCount = tabs.size
            ) { page ->
                // Each page is told whether it is the one on screen, because the pager
                // composes the page being dragged in as well. The two that read every
                // installed package - six hundred of them, twice over in Manage's case -
                // started that the moment they were half on screen, which is what made
                // swiping towards the shell stutter: the swipe passes straight through both
                // app lists on its way there. A page that is not the settled one now waits,
                // and loads when it is landed on.
                val active = page == pagerState.settledPage
                    // 每页独立的 ScrollBehavior 与 backdrop：列表滚动折叠本页顶栏，
                    // 顶栏从内容层做渐进式模糊，切页互不影响。
                    // 应用页的顶栏下拉是玻璃菜单（GlassIconDropdownMenu），它要采样内容层
                    // 才能出玻璃质感，所以这里也一并开启；该页内容根已挂 LocalContentBackdrop。
                    CollapsibleTopAppBarHost(
                        modifier = Modifier.fillMaxSize(),
                        blur = true,
                    ) {
                    when (page) {
                        0 -> HomeScreen(bottomPadding = bottomPadding)
                        1 -> AppsScreen(
                            bottomPadding = bottomPadding,
                            active = active,
                            warmUp = warmUp,
                            onGrantedCount = { grantedCount = it }
                        )
                        2 -> ModulesScreen(
                            bottomPadding = bottomPadding,
                            active = active,
                            onFullscreenChange = { modulesFullscreen = it }
                        )
                        3 -> LabsScreen(bottomPadding = bottomPadding, onOpenDetail = onOpenDetail)
                        4 -> SettingsScreen(
                            bottomPadding = bottomPadding,
                            onOpenDetail = onOpenDetail,
                            onRunSetup = onRunSetup,
                            showNavigationBarSetting = true,
                            alwaysShowNavigationBar = alwaysShowNavigationBar,
                            onAlwaysShowNavigationBarChange = onAlwaysShowNavigationBarChange,
                            onSubPageChange = { settingsSubPage = it }
                        )
                    }
                }
            }
        }
        }   // 关闭记录 backdrop 的内容层

        // 设置子页面只在设置 tab 上隐藏底栏；滑去别的 tab 底栏照常出现。
        if (!modulesFullscreen && !(settingsSubPage && pagerState.currentPage == 4)) {
            // 官方 GlassNavigationBar：它自己带 defaultWindowInsetsPadding 与 fillMaxWidth，
            // 靠 align(BottomCenter) 叠在内容之上。
            // 关键：它必须是 backdrop 内容层的**兄弟**（绘制在其后），这样它采样到的
            // 才是背后真实的内容；若放进内容层内部，采样源里就只有它自己。
            GlassNavigationBar(
                items = tabs.mapIndexed { index, tab ->
                    val label = stringResource(tab.label)
                    GlassNavigationItem(
                        icon = tab.selectedIcon,
                        label = label,
                        contentDescription = label,
                        badge = if (index == 1 && grantedCount > 0) {
                            {
                                // Demo 的 Badge 是贴图标右上角的小圆，文字用 footnote2，
                                // 不能用默认 body 字号，否则会把圆撑大。
                                Badge {
                                    Text(
                                        text = grantedCount.toString(),
                                        style = MiuixTheme.textStyles.footnote2,
                                    )
                                }
                            }
                        } else null,
                    )
                },
                selectedIndex = pagerState.currentPage,
                onSelect = { index ->
                    if (index != pagerState.currentPage) {
                        scope.launch { pagerState.scrollToPage(index) }
                    }
                },
                backdrop = backdrop,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

/**
 * The overscan a TV may cut from every edge. Televisions are free to crop the picture, and the
 * guidance for what is always safe to draw in is about five percent of the screen: 48dp at the
 * sides and 27dp at the top and bottom of a 960 x 540dp screen.
 */
private val TelevisionSafeHorizontal = 48.dp
private val TelevisionSafeVertical = 27.dp

/**
 * The tabs on a TV: the same five pages, with the bar as a column down the left edge instead
 * of a row floating along the bottom.
 *
 * The floating bar is a thumb's bar. It hides as a page scrolls, which a remote has no gesture
 * to undo, and it can only be reached by walking the focus down past everything on the page.
 * A rail on the left is always there and always one press away: left from anywhere on a page.
 *
 * Only the selected page is composed. The phone keeps every page alive in the pager so that
 * swiping is cheap, but a page that is composed is also a page the D-pad can move focus into,
 * and on a TV it did: focus walked off the page on screen into one that was not, the pager
 * followed it, and once it was stranded there the remote appeared to do nothing at all. With
 * one page there is nowhere for focus to go but what is visible. Each page's scroll position
 * is filed under its own key in [pages], so leaving a tab and coming back still lands where it
 * was left.
 */
@Composable
private fun TelevisionTabs(
    selected: Int,
    onSelect: (Int) -> Unit,
    pages: SaveableStateHolder,
    focusPageFirst: Boolean,
    returnFocus: TelevisionReturn,
    onOpenDetail: (Detail) -> Unit,
    onRunSetup: () -> Unit
) {
    var grantedCount by remember { mutableIntStateOf(0) }
    val railItems = remember { List(tabs.size) { FocusRequester() } }
    val page = remember { FocusRequester() }

    // Back from a page goes to the rail first, the way a TV app's back does: left only gets
    // there once nothing on the page is further left, which on a wide page can be a while.
    // From the rail, back leaves the app as it always has. A page's own back - a module's full
    // screen - is composed after this one, so it still comes first.
    var pageFocused by remember { mutableStateOf(false) }

    // The tab follows the focus, so walking the rail from Home to Labs passes through Apps on
    // the way - and Apps reads every installed package the moment it is active, a binder call
    // that cannot be called back once it has started. It is let go only once the focus has
    // stayed on Apps for a moment, the same way the phone's pager waits for the swipe to settle.
    var appsActive by remember { mutableStateOf(false) }
    LaunchedEffect(selected) {
        appsActive = false
        if (selected == 1) {
            delay(300)
            appsActive = true
        }
    }

    // Whether the focus was last on the page rather than the rail, and whether it is leaving
    // the page because it was sent - left, or back. A page can take away the thing that held
    // the focus: Find modules opens over the Modules list, and the button that opened it is
    // gone. The platform then gives the focus to the first thing it finds, which is the rail,
    // and the next press down moved on from the open tab to the next one. So focus that turns
    // up on the rail without having been sent there is given back to the page, once the frame
    // that changed the page is over, at whatever is now first on it.
    var focusOnPage by remember { mutableStateOf(false) }
    var leavingPage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val returnToPage: () -> Unit = {
        scope.launch {
            // Two frames: one for the old item to go, one for what replaces it.
            withFrameNanos {}
            withFrameNanos {}
            // A page with nothing on it to focus gives the rail the focus for good, or every
            // move along the rail would be taken for the page handing focus away again.
            if (focusOnPage && !pageFocused && !page.requestFocus()) focusOnPage = false
        }
    }

    // Somewhere for the focus to start. With nothing focused the first press only found the
    // rail on its way somewhere else - down from nowhere landed on Apps - so the open tab takes
    // the focus as the tabs appear; or the page does, when they appear again because a detail
    // screen opened from it has closed, which is where the focus was when it opened.
    LaunchedEffect(Unit) {
        withFrameNanos {}
        if (focusPageFirst) {
            focusOnPage = true
            // A frame for the item that opened the detail to take the focus back itself (see
            // televisionReturnFocus); the page's first item only if it could not. Either way the
            // claim is over after this, so an item scrolled into view later cannot take it.
            withFrameNanos {}
            returnFocus.detail = null
            if (!pageFocused && !page.requestFocus()) railItems[selected].requestFocus()
        } else {
            railItems[selected].requestFocus()
        }
    }

    BackHandler(enabled = pageFocused) {
        leavingPage = true
        railItems[selected].requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = TelevisionSafeVertical)
    ) {
        NavigationRail(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = TelevisionSafeHorizontal - 24.dp)
                .focusProperties {
                    onEnter = {
                        if (requestedFocusDirection == FocusDirection.Left) leavingPage = true
                        railItems[selected].requestFocus()
                    }
                    onExit = {
                        when {
                            requestedFocusDirection.isVertical -> cancelFocusChange()
                            requestedFocusDirection == FocusDirection.Right -> page.requestFocus()
                        }
                    }
                }
                .focusGroup(),
            color = Color.Transparent,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            tabs.forEachIndexed { index, tab ->
                val isSelected = selected == index
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onSelect(index) },
                    icon = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                    label = stringResource(tab.label),
                    modifier = Modifier
                        .focusRequester(railItems[index])
                        .onFocusChanged {
                            if (!it.isFocused) return@onFocusChanged
                            if (focusOnPage && !leavingPage) {
                                returnToPage()
                            } else {
                                focusOnPage = false
                                onSelect(index)
                            }
                            leavingPage = false
                        }
                        .televisionFocusShape(null),
                    badge = if (index == 1 && grantedCount > 0) {
                        {
                            Badge {
                                Text(
                                    text = grantedCount.toString(),
                                    style = MiuixTheme.textStyles.footnote2,
                                )
                            }
                        }
                    } else null,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        CollapsibleTopAppBarHost(modifier = Modifier.weight(1f)) {
        CenteredContent(
            modifier = Modifier
                .padding(end = TelevisionSafeHorizontal)
                .onFocusChanged {
                    pageFocused = it.hasFocus
                    if (it.hasFocus) {
                        focusOnPage = true
                    } else if (focusOnPage) {
                        // Lost to nothing at all, which a page can also do: given back the same
                        // way. Focus that went to the rail on purpose has cleared focusOnPage by
                        // the time this looks.
                        returnToPage()
                    }
                }
                // Asked for as a whole, a group hands focus to its first item.
                .focusRequester(page)
                .focusProperties {
                    onExit = { if (requestedFocusDirection.isVertical) cancelFocusChange() }
                }
                .focusGroup()
        ) {
            pages.SaveableStateProvider("$TAB_PAGES-$selected") {
                CompositionLocalProvider(LocalTelevisionReturn provides returnFocus) {
                when (selected) {
                    0 -> HomeScreen(bottomPadding = 0.dp)
                    1 -> AppsScreen(
                        bottomPadding = 0.dp,
                        active = appsActive,
                        warmUp = false,
                        onGrantedCount = { grantedCount = it }
                    )
                    // The rail stays when a module opens one of its full screens: on a TV it
                    // is the way to the other tabs, and it covers nothing of the module's.
                    2 -> ModulesScreen(
                        bottomPadding = 0.dp,
                        active = true,
                        onFullscreenChange = {}
                    )
                    3 -> LabsScreen(bottomPadding = 0.dp, onOpenDetail = onOpenDetail)
                    4 -> SettingsScreen(
                        bottomPadding = 0.dp,
                        onOpenDetail = onOpenDetail,
                        onRunSetup = onRunSetup
                    )
                }
                }
            }
        }
        }
    }
}

/**
 * The detail screen the TV tabs last opened, until the tabs are back and the item that opened it
 * has had its chance to take the focus again.
 */
class TelevisionReturn {
    var detail: Detail? = null
}

private val LocalTelevisionReturn = staticCompositionLocalOf<TelevisionReturn?> { null }

/**
 * On a TV, the focus comes back to this item when the detail screen it opened closes.
 *
 * A detail screen takes the whole window, so the tab it was opened from leaves the composition
 * and is built again on the way back. The page's first item taking the focus then is better than
 * nothing taking it, but it is not where anyone was: back from Log landed on Activities Launcher,
 * a row and four tiles away. Put this on the item that opens [detail], before its clickable.
 * Off a TV it is nothing.
 */
@Composable
fun Modifier.televisionReturnFocus(detail: Detail): Modifier {
    val target = LocalTelevisionReturn.current ?: return this
    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // A frame for the page to be laid out, so the item is in place to be focused.
        withFrameNanos {}
        if (target.detail == detail) {
            target.detail = null
            requester.requestFocus()
        }
    }
    return focusRequester(requester)
}

private val FocusDirection.isVertical: Boolean
    get() = this == FocusDirection.Up || this == FocusDirection.Down
