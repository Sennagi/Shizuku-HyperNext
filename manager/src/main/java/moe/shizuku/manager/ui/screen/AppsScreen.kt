package moe.shizuku.manager.ui.screen

import android.util.Log
import moe.shizuku.manager.ui.component.PillButton
import moe.shizuku.manager.ui.component.PillButtonQuiet
import moe.shizuku.manager.AppConstants
import android.content.pm.PackageInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import moe.shizuku.manager.ui.miuix.AlertDialog
import moe.shizuku.manager.ui.miuix.Button
import moe.shizuku.manager.ui.miuix.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import moe.shizuku.manager.ui.miuix.HorizontalDivider
import moe.shizuku.manager.ui.miuix.Icon
import moe.shizuku.manager.ui.miuix.IconButton
import moe.shizuku.manager.ui.miuix.ListItem
import androidx.compose.material3.ListItemDefaults
import moe.shizuku.manager.ui.miuix.LoadingIndicator
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import moe.shizuku.manager.ui.miuix.IconDropdownMenu
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarResult
import moe.shizuku.manager.ui.component.RefreshBox
import moe.shizuku.manager.ui.component.TelevisionRefreshButton
import moe.shizuku.manager.ui.component.ExpressiveSwitch
import moe.shizuku.manager.ui.miuix.Text
import moe.shizuku.manager.ui.miuix.LocalContentBackdrop
import moe.shizuku.manager.ui.miuix.topAppBarContentTopPadding
import moe.shizuku.manager.ui.miuix.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.authorization.AuthorizationManager
import moe.shizuku.manager.authorization.ClientProcessInfo
import moe.shizuku.manager.authorization.GrantAuthenticator
import moe.shizuku.manager.manage.InstalledPackages
import moe.shizuku.manager.receiver.ShizukuReceiverStarter
import moe.shizuku.manager.ui.component.AppIcon
import moe.shizuku.manager.ui.component.packageLine
import moe.shizuku.manager.ui.component.rowKey
import moe.shizuku.manager.ui.component.AppRowLine
import moe.shizuku.manager.ui.component.AppStatusChips
import moe.shizuku.manager.ui.component.ProcessCountChip
import moe.shizuku.manager.ui.component.CenteredMessage
import moe.shizuku.manager.ui.component.SegmentedCard
import moe.shizuku.manager.ui.component.stripHtmlTags
import moe.shizuku.manager.utils.ShizukuStateMachine

enum class SortOrder {
    /** Most recently installed first: when the app first appeared on the device. */
    RECENTLY_INSTALLED,

    /** Most recently updated first: when the APK last changed. */
    RECENTLY_UPDATED,

    ALPHABETICAL
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppsScreen(
    bottomPadding: Dp,
    active: Boolean = true,
    warmUp: Boolean = false,
    // Reported so the tab bar can carry it as a badge: the count is worked out here, over the
    // list the server just handed over, and nowhere else has that list.
    onGrantedCount: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val pm = context.packageManager

    var all by remember { mutableStateOf<List<PackageInfo>>(emptyList()) }
    var sortOrder by remember { mutableStateOf(SortOrder.RECENTLY_INSTALLED) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var version by remember { mutableIntStateOf(0) }
    var pendingBatch by remember { mutableStateOf<Boolean?>(null) }
    var permissionLimited by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var running by remember { mutableStateOf(ShizukuStateMachine.isRunning()) }
    // Pull-to-refresh. While this is set the list is being read again behind the rows
    // already on screen, so the gesture's own indicator stands in for the page spinner
    // and the list never blinks back to blank.
    var refreshing by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    // Attached client processes, per app uid. These are read with the app list and expanded
    // inline, so this screen does not need a separate temporary-grants tab or filter.
    var clientProcessesByUid by remember { mutableStateOf<Map<Int, List<ClientProcessInfo>>>(emptyMap()) }
    var expandedProcessUid by remember { mutableStateOf<Int?>(null) }
    var pendingStopProcess by remember { mutableStateOf<ClientProcessInfo?>(null) }

    // The list comes from the server, so it has to be re-read when that comes or goes
    // (and the answer while it is down is "there is nothing to list", not an empty page).
    DisposableEffect(Unit) {
        val listener: (ShizukuStateMachine.State) -> Unit = {
            running = it == ShizukuStateMachine.State.RUNNING
        }
        ShizukuStateMachine.addListener(listener)
        onDispose { ShizukuStateMachine.removeListener(listener) }
    }

    // Read once per version: either when this is the tab on screen, or once the app has
    // settled after launch, so that the swipe towards it stays cheap and the first visit does
    // not wait for the server to be asked about every package.
    var loadedFor by remember { mutableStateOf<Pair<Boolean, Int>?>(null) }
    LaunchedEffect(running, active, warmUp, refreshKey, version) {
        if (!active && !warmUp) return@LaunchedEffect
        // A pull starts from a list that is already on screen, so it is the one case that
        // skips the guard: reading again is the whole point of the gesture. Every other
        // trigger still waits until its version is the one that was read.
        if (!refreshing && loadedFor == (running to version)) return@LaunchedEffect

        loading = true
        Log.d(AppConstants.TAG, "Apps: reading the authorised apps")
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                AuthorizationManager.getPackages(exclude = listOf(context.packageName))
            }.getOrDefault(emptyList())
        }
        all = loaded
        clientProcessesByUid = withContext(Dispatchers.IO) {
            val uids = loaded.mapNotNull { it.applicationInfo?.uid }.distinct().toIntArray()
            if (uids.isEmpty()) {
                emptyMap()
            } else {
                AuthorizationManager.getClientProcesses(uids).groupBy { it.uid }
            }
        }
        expandedProcessUid = null
        loadedFor = running to version
        loading = false
        refreshing = false
    }

    // Both sets are worked out once per loaded list, off the main thread: the granted one is
    // a server round trip per app, and the chips want its size before anyone picks that
    // filter. Recomputed when a toggle changes something (version), which is what keeps the
    // counts honest.
    var permanentGrantedNames by remember { mutableStateOf(emptySet<String>()) }
    var launcherless by remember { mutableStateOf(emptySet<String>()) }
    var derivedFor by remember { mutableStateOf<Pair<Boolean, Int>?>(null) }
    LaunchedEffect(all, version, active, warmUp) {
        if ((!active && !warmUp) || all.isEmpty()) {
            permanentGrantedNames = emptySet()
            launcherless = emptySet()
            return@LaunchedEffect
        }
        if (derivedFor == (running to version)) return@LaunchedEffect
        val (granted, withoutLauncher) = withContext(Dispatchers.IO) {
            val granted = all.filter {
                val uid = it.applicationInfo?.uid ?: return@filter false
                runCatching { AuthorizationManager.granted(it.packageName, uid) }.getOrDefault(false)
            }.map { it.packageName }.toSet()

            // An app with no launcher entry is what "hidden" means: it is in the list, but
            // never in the app drawer. The leanback category counts as an entry on TV.
            val withoutLauncher = InstalledPackages.launcherless(context, all)

            granted to withoutLauncher
        }
        permanentGrantedNames = granted
        launcherless = withoutLauncher
    }

    // A temporary grant lives in the server's attached process, not in the saved flags. Its app
    // is still holding access, so the Granted filter and the tab count include it. The row keeps
    // the switch off because there is no permanent grant to switch off.
    val oneTimeNames = remember(all, clientProcessesByUid, permanentGrantedNames) {
        val namesByUid = all.mapNotNull { pi ->
            pi.applicationInfo?.uid?.let { uid -> uid to pi.packageName }
        }.toMap()
        clientProcessesByUid
            .filterValues { processes -> processes.any { it.allowed } }
            .keys
            .mapNotNull { namesByUid[it] }
            .filterNot { it in permanentGrantedNames }
            .toSet()
    }
    val grantedNames = permanentGrantedNames + oneTimeNames

    LaunchedEffect(grantedNames.size) { onGrantedCount(grantedNames.size) }

    val shown = remember(all, sortOrder) {
        when (sortOrder) {
            // The two timestamps the package manager keeps: when it was installed, and when
            // the APK was last replaced. They differ for anything that has been updated.
            SortOrder.RECENTLY_INSTALLED -> all.sortedByDescending { it.firstInstallTime }
            SortOrder.RECENTLY_UPDATED -> all.sortedByDescending { it.lastUpdateTime }
            SortOrder.ALPHABETICAL -> all.sortedBy {
                runCatching { it.applicationInfo?.loadLabel(pm)?.toString()?.lowercase() }
                    .getOrDefault(it.packageName)
            }
        }
    }

    val selectionMode = selected.isNotEmpty()
    val scope = rememberCoroutineScope()
    // Remembered for the whole screen rather than per batch, so a second batch replaces
    // the first offer to undo instead of stacking snackbars.
    val snackbarHostState = remember { SnackbarHostState() }

    fun toggleProcesses(uid: Int) {
        expandedProcessUid = if (expandedProcessUid == uid) null else uid
    }

    fun stopProcess(process: ClientProcessInfo) {
        pendingStopProcess = null
        scope.launch {
            val stopped = withContext(Dispatchers.IO) {
                AuthorizationManager.stopClientProcess(process.uid, process.pid)
            }
            if (!stopped) {
                snackbarHostState.showSnackbar(context.getString(R.string.apps_process_stop_failed))
                return@launch
            }

            val remaining = clientProcessesByUid[process.uid].orEmpty()
                .filterNot { it.pid == process.pid }
            clientProcessesByUid = clientProcessesByUid + (process.uid to remaining)
            if (remaining.none { it.allowed }) expandedProcessUid = null
            version++
        }
    }

    /**
     * Changes one app's grant, asking for the device's authentication first when that grant is
     * being created. Revoking needs no proof: it only takes access away.
     */
    fun updateGrant(pi: PackageInfo, grant: Boolean) {
        if (!grant) {
            val result = runCatching { AuthorizationManager.revoke(pi.packageName, pi.applicationInfo!!.uid) }
            if (result.exceptionOrNull() is SecurityException) permissionLimited = true
            version++
            return
        }

        val host = activity ?: return
        scope.launch {
            if (!GrantAuthenticator.authenticate(host)) return@launch
            val result = withContext(Dispatchers.IO) {
                runCatching { AuthorizationManager.grant(pi.packageName, pi.applicationInfo!!.uid) }
            }
            if (result.exceptionOrNull() is SecurityException) permissionLimited = true
            version++
        }
    }

    /**
     * Applies a batch and offers to take it back.
     *
     * Flipping every listed app is one tap and one confirmation, which is a lot of
     * permission to change by accident, so the snackbar that follows carries Undo and
     * puts the same apps back the way they were. A batch the server refused is not
     * reversible and gets no offer to undo it.
     */
    fun applyBatch(grant: Boolean, apps: List<PackageInfo>) {
        scope.launch {
            val host = activity ?: return@launch
            if (grant && !GrantAuthenticator.authenticate(host)) return@launch
            val limited = withContext(Dispatchers.IO) {
                var limited = false
                for (pi in apps) {
                    val result = runCatching {
                        if (grant) AuthorizationManager.grant(pi.packageName, pi.applicationInfo!!.uid)
                        else AuthorizationManager.revoke(pi.packageName, pi.applicationInfo!!.uid)
                    }
                    if (result.exceptionOrNull() is SecurityException) limited = true
                }
                limited
            }
            if (limited) permissionLimited = true
            version++
            if (limited) return@launch

            val result = snackbarHostState.showSnackbar(
                message = context.getString(
                    if (grant) R.string.app_management_batch_granted
                    else R.string.app_management_batch_revoked,
                    apps.size
                ),
                actionLabel = context.getString(R.string.action_undo),
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                // Turning a revoke back into a grant is a new grant, so it is authenticated too.
                if (!grant && !GrantAuthenticator.authenticate(host)) return@launch
                // Back the other way, over the same apps.
                withContext(Dispatchers.IO) {
                    for (pi in apps) {
                        runCatching {
                            if (grant) AuthorizationManager.revoke(pi.packageName, pi.applicationInfo!!.uid)
                            else AuthorizationManager.grant(pi.packageName, pi.applicationInfo!!.uid)
                        }
                    }
                }
                version++
            }
        }
    }

    val refresh: () -> Unit = {
        refreshing = true
        // The derived sets are worked out per list, so a re-read has to ask for
        // them again too: a pull is how you pick up a grant made elsewhere.
        derivedFor = null
        refreshKey++
    }

    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        // 内容铺满整页（含顶栏下方），记录进 backdrop；顶栏叠加在上方采样它做渐进模糊。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(LocalContentBackdrop.current)
        ) {
        RefreshBox(
            isRefreshing = refreshing,
            onRefresh = refresh,
            modifier = Modifier.fillMaxSize(),
            // 刷新头 offset 到列表第一项的位置，别停在状态栏下方压住大字标题。
            contentPadding = PaddingValues(top = topAppBarContentTopPadding())
        ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                top = topAppBarContentTopPadding(),
                end = 16.dp,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shown, key = { it.rowKey() }) { pi ->
                val uid = pi.applicationInfo!!.uid
                val permanentGranted = remember(pi.packageName, version) {
                    runCatching { AuthorizationManager.granted(pi.packageName, uid) }.getOrDefault(false)
                }
                val oneTime = pi.packageName in oneTimeNames
                // The switch is on while either kind of access is held, so turning it off
                // revokes the temporary grant as well as the permanent one.
                val granted = permanentGranted || oneTime
                val isSelected = pi.packageName in selected
                val processes = clientProcessesByUid[uid].orEmpty().filter { it.allowed }

                SegmentedCard(
                    // A selected row tints its card, so a multi-select pass reads at a
                    // glance instead of needing the checkbox to be spotted each time.
                    color = if (selectionMode && isSelected) {
                        MiuixTheme.colorScheme.secondaryContainer
                    } else {
                        MiuixTheme.colorScheme.surfaceContainerHigh
                    }
                ) {
                    Column {
                        ListItem(
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    if (selectionMode) {
                                        selected = if (isSelected) selected - pi.packageName else selected + pi.packageName
                                    } else {
                                        updateGrant(pi, !granted)
                                    }
                                },
                                onLongClick = {
                                    selected = if (isSelected) selected - pi.packageName else selected + pi.packageName
                                }
                            ),
                            leadingContent = { AppIcon(pi) },
                            headlineContent = {
                                Text(
                                    runCatching { pi.applicationInfo!!.loadLabel(pm).toString() }
                                        .getOrDefault(pi.packageName)
                                )
                            },
                            // The name has the whole first line now, so a long one no longer wraps to
                            // make room for the chip and the switch: they moved to the line under it,
                            // with the package name giving way instead.
                            supportingContent = {
                                AppRowLine(pi.packageLine()) {
                                    // The process count comes first when there is one, then the
                                    // same chips every app list carries.
                                    if (!selectionMode && processes.isNotEmpty()) {
                                        ProcessCountChip(
                                            count = processes.size,
                                            expanded = expandedProcessUid == uid,
                                            onClick = { toggleProcesses(uid) }
                                        )
                                    }
                                    AppStatusChips(
                                        pi,
                                        hidden = pi.packageName in launcherless,
                                        oneTime = oneTime
                                    )
                                    if (selectionMode) {
                                        Checkbox(checked = isSelected, onCheckedChange = null)
                                    } else {
                                        ExpressiveSwitch(
                                            checked = granted,
                                            onCheckedChange = { checked -> updateGrant(pi, checked) }
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        if (expandedProcessUid == uid && processes.isNotEmpty()) {
                            HorizontalDivider(color = MiuixTheme.colorScheme.dividerLine)
                            processes.forEach { process ->
                                ClientProcessRow(
                                    process = process,
                                    onStop = { pendingStopProcess = process }
                                )
                            }
                        }
                    }
                }
            }
        }

        // While the first read is in flight the rows are stood in for rather than left as
        // a blank page with a spinner over it; the list keeps its shape and the wait is
        // spent looking at where the content will be. A pull is not this case: the rows
        // already there stay, which is why the skeleton asks for an empty list as well.
        if (loading && shown.isEmpty()) {
            CenteredMessage { LoadingIndicator() }
        }

        // Say why the page is empty: no server to ask, or genuinely no apps —
        // a blank page on its own explains nothing.
        if (!loading && shown.isEmpty()) {
            CenteredMessage {
                when {
                    !running -> {
                        Text(
                            text = stringResource(R.string.apps_needs_shizuku),
                            style = MiuixTheme.textStyles.body2,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            modifier = Modifier.padding(top = 12.dp),
                            onClick = {
                                ShizukuReceiverStarter.start(context, userInitiated = true, reason = "apps-screen")
                            }
                        ) { Text(stringResource(R.string.action_start)) }
                    }

                    else -> Text(
                        text = stringResource(R.string.apps_empty),
                        style = MiuixTheme.textStyles.body2,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        }
        }

        if (selectionMode) {
            TopAppBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                title = stringResource(R.string.batch_selected_count, selected.size),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                navigationIcon = {
                    IconButton(onClick = { selected = emptySet() }) {
                        Icon(MiuixIcons.Close, contentDescription = null)
                    }
                },
                actions = {
                    PillButtonQuiet(onClick = { selected = shown.map { it.packageName }.toSet() }) {
                        Text(stringResource(R.string.app_management_action_select_all))
                    }
                    PillButtonQuiet(onClick = { pendingBatch = true }) {
                        Text(stringResource(R.string.app_management_action_grant))
                    }
                    PillButtonQuiet(onClick = { pendingBatch = false }) {
                        Text(stringResource(R.string.app_management_action_revoke))
                    }
                }
            )
        } else {
            TopAppBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                title = stringResource(R.string.tab_apps),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                actions = {
                    TelevisionRefreshButton(refreshing, refresh)
                    IconDropdownMenu(
                        entry = DropdownEntry(
                            items = listOf(
                                DropdownItem(
                                    text = stringResource(R.string.app_management_sort_last_added),
                                    selected = sortOrder == SortOrder.RECENTLY_INSTALLED,
                                    onClick = { sortOrder = SortOrder.RECENTLY_INSTALLED },
                                ),
                                DropdownItem(
                                    text = stringResource(R.string.app_management_sort_updated),
                                    selected = sortOrder == SortOrder.RECENTLY_UPDATED,
                                    onClick = { sortOrder = SortOrder.RECENTLY_UPDATED },
                                ),
                                DropdownItem(
                                    text = stringResource(R.string.app_management_sort_alphabetical),
                                    selected = sortOrder == SortOrder.ALPHABETICAL,
                                    onClick = { sortOrder = SortOrder.ALPHABETICAL },
                                ),
                            ),
                        ),
                    ) {
                        Icon(MiuixIcons.Sort, contentDescription = null)
                    }
                }
            )
        }

    // The undo offer sits above the list and above the floating bar, which is drawn over
    // the page rather than beside it, so the same padding the list uses to clear the bar is
    // what keeps the snackbar from appearing underneath it.
    SnackbarHost(
        state = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding + 16.dp)
    )

    }

    if (permissionLimited) {
        val adbUrl = runCatching { Helps.ADB.get() }.getOrDefault("")
        AlertDialog(
            onDismissRequest = { permissionLimited = false },
            title = { Text(stringResource(R.string.app_management_dialog_adb_is_limited_title)) },
            text = {
                // The string is written for the View UI, which parses its markup: here the tags
                // were drawn as tags. It links the document it names, and the link is inside the
                // markup, so the address is printed under the sentence rather than lost with it.
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.app_management_dialog_adb_is_limited_message, adbUrl)
                            .stripHtmlTags()
                    )
                    if (adbUrl.isNotEmpty()) {
                        Text(
                            adbUrl,
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                PillButton(onClick = { permissionLimited = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }

    pendingBatch?.let { grant ->
        AlertDialog(
            onDismissRequest = { pendingBatch = null },
            title = {
                Text(
                    stringResource(
                        if (grant) R.string.app_management_batch_grant_title
                        else R.string.app_management_batch_revoke_title
                    )
                )
            },
            text = { Text(stringResource(R.string.app_management_batch_message, selected.size)) },
            confirmButton = {
                PillButton(onClick = {
                    // Same path as Toggle all, so a hand-picked batch can be taken back
                    // just as easily.
                    val target = all.filter { it.packageName in selected }
                    pendingBatch = null
                    selected = emptySet()
                    applyBatch(grant, target)
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                PillButtonQuiet(onClick = { pendingBatch = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    pendingStopProcess?.let { process ->
        val label = all.firstOrNull { it.applicationInfo?.uid == process.uid }
            ?.let { runCatching { it.applicationInfo?.loadLabel(pm)?.toString() }.getOrNull() }
            ?: process.packageName.ifBlank { "uid ${process.uid}" }
        AlertDialog(
            onDismissRequest = { pendingStopProcess = null },
            title = { Text(stringResource(R.string.apps_process_stop_title)) },
            text = { Text(stringResource(R.string.apps_process_stop_message, process.pid, label)) },
            confirmButton = {
                PillButton(onClick = { stopProcess(process) }) {
                    Text(stringResource(R.string.apps_process_stop))
                }
            },
            dismissButton = {
                PillButtonQuiet(onClick = { pendingStopProcess = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}
