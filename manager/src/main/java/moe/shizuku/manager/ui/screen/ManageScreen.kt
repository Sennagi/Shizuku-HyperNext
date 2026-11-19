package moe.shizuku.manager.ui.screen

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.util.Log
import moe.shizuku.manager.ui.component.televisionTextInput
import moe.shizuku.manager.AppConstants
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.PushPin
import top.yukonga.miuix.kmp.theme.MiuixTheme
import moe.shizuku.manager.ui.miuix.AlertDialog
import moe.shizuku.manager.ui.miuix.Checkbox
import moe.shizuku.manager.ui.miuix.DropdownMenu
import moe.shizuku.manager.ui.miuix.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import moe.shizuku.manager.ui.miuix.FilterChip
import moe.shizuku.manager.ui.miuix.Icon
import moe.shizuku.manager.ui.miuix.IconButton
import moe.shizuku.manager.ui.miuix.LinearProgressIndicator
import moe.shizuku.manager.ui.miuix.ListItem
import androidx.compose.material3.ListItemDefaults
import moe.shizuku.manager.ui.miuix.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import moe.shizuku.manager.ui.miuix.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import moe.shizuku.manager.ui.miuix.Surface
import moe.shizuku.manager.ui.miuix.Text
import moe.shizuku.manager.ui.miuix.TopAppBar
import moe.shizuku.manager.ui.component.RefreshBox
import moe.shizuku.manager.ui.component.TelevisionRefreshButton
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.R
import moe.shizuku.manager.manage.APP_OPS
import moe.shizuku.manager.manage.AppOpChoice
import moe.shizuku.manager.manage.InstalledPackages
import moe.shizuku.manager.manage.OpMode
import moe.shizuku.manager.manage.PackageTools
import moe.shizuku.manager.manage.PinnedApps
import moe.shizuku.manager.ui.component.AppFilterChip
import moe.shizuku.manager.ui.component.appLabel
import moe.shizuku.manager.ui.component.packageLine
import moe.shizuku.manager.ui.component.rowKey
import moe.shizuku.manager.ui.component.AppIcon
import moe.shizuku.manager.ui.component.AppRowLine
import moe.shizuku.manager.ui.component.AppStatusChips
import moe.shizuku.manager.ui.component.CenteredMessage
import moe.shizuku.manager.ui.component.ChoiceRow
import moe.shizuku.manager.ui.component.ChipEmphasis
import moe.shizuku.manager.ui.component.PillButton
import moe.shizuku.manager.ui.component.PillButtonQuiet
import moe.shizuku.manager.ui.component.SegmentedCard
import moe.shizuku.manager.ui.component.SegmentedListItem
import moe.shizuku.manager.ui.component.StatusChip

/** Which slice of the installed apps to list. */
enum class ManageFilter {
    ALL,

    /** Apps the user brought, which are the safe ones to change. */
    USER,

    /** Apps that came with the system image, where changing anything is riskier. */
    SYSTEM,

    /**
     * Apps that are installed but not running: disabled with the package manager, or
     * suspended. Both mean the same thing to whoever is looking for them here.
     */
    DISABLED,

    /**
     * Apps with no launcher entry, so they never appear in the app drawer services,
     * system pieces and background components. The same meaning the Apps tab gives it.
     */
    HIDDEN
}

private data class BulkTarget(
    val packageName: String,
    val label: String
)

private data class BulkRequest(
    val choice: AppOpChoice,
    val mode: OpMode,
    val targets: List<BulkTarget>
)

private data class BulkOutcome(
    val packageName: String,
    val label: String,
    val changed: Boolean
)

private data class BulkReport(
    val choice: AppOpChoice,
    val mode: OpMode,
    val outcomes: List<BulkOutcome>
)

private sealed interface BulkState {
    data class Confirm(val request: BulkRequest) : BulkState
    data class Running(val request: BulkRequest, val completed: Int) : BulkState
    data class Finished(val report: BulkReport) : BulkState
}

/**
 * The Manage tab: every installed app, and behind each one the things that are normally
 * only reachable from a computer system permissions, app ops, run state and battery
 * policy.
 *
 * Unlike the Apps tab this list is read straight from the local package manager, so it
 * works with Shizuku stopped; only the rows that change something need the server, and
 * they say so instead of failing.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManageScreen(
    bottomPadding: Dp,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val pm = context.packageManager

    var apps by remember { mutableStateOf<List<PackageInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    // Pull-to-refresh. While this is set the list is being read again behind the rows
    // already on screen, so the gesture's own indicator stands in for the page spinner
    // and the list never blinks back to blank.
    var refreshing by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.ALPHABETICAL) }
    var sortMenu by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(ManageFilter.ALL) }
    // The open app is remembered across rotation, like the tab itself: coming back should
    // not drop you out of the app you were reading.
    var openPackage by rememberSaveable { mutableStateOf<String?>(null) }
    // Bumped whenever something changed, to re-read the list (a removed app belongs under
    // a different filter afterwards).
    var version by remember { mutableIntStateOf(0) }
    // Selecting apps is its own mode: while it is open the rows pick apps instead of
    // opening them, and the batch editor applies one app op to the whole set.
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var pinned by remember { mutableStateOf(PinnedApps.all()) }
    var bulkPicker by remember { mutableStateOf(false) }
    var bulkState by remember { mutableStateOf<BulkState?>(null) }
    val scope = rememberCoroutineScope()

    // Which version the list has been read for, so that opening this screen twice cannot read
    // it twice: the second read would put the cost back on a screen that already has an
    // answer.
    var loadedFor by remember { mutableIntStateOf(-1) }

    // Reading every installed package, and then asking about each one, is the most expensive
    // thing this app does, which is why it happens on open rather than for a page nobody asked
    // for: this screen is a tile on the Labs tab, so it exists only once somebody opened it,
    // and the placeholder rows stand in while it reads.
    LaunchedEffect(version, refreshKey) {
        // A pull starts from a list that is already on screen, so it is the one case that
        // skips the guard: reading again is the whole point of the gesture. Every other
        // trigger still waits until its version is the one that was read.
        if (!refreshing && loadedFor == version) return@LaunchedEffect

        loading = true
        Log.d(AppConstants.TAG, "Manage: reading the installed packages")
        apps = withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            InstalledPackages.all(context, PackageManager.MATCH_UNINSTALLED_PACKAGES)
        }
        loadedFor = version
        loading = false
        refreshing = false
    }

    // An app that is no longer installed keeps its row but has no application record, so it
    // belongs to neither half it stays reachable through All, where its row says so.
    val systemPackages = remember(apps) {
        apps.filter { (it.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0 }
            .map { it.packageName }.toSet()
    }
    val userPackages = remember(apps, systemPackages) {
        apps.filter { it.applicationInfo != null && it.packageName !in systemPackages }
            .map { it.packageName }.toSet()
    }
    val disabledPackages = remember(apps) {
        apps.filter {
            val ai = it.applicationInfo ?: return@filter false
            !ai.enabled || (ai.flags and ApplicationInfo.FLAG_SUSPENDED) != 0
        }.map { it.packageName }.toSet()
    }
    val removedPackages = remember(apps) {
        apps.filter { it.applicationInfo == null }.map { it.packageName }.toSet()
    }

    // Worked out off the main thread, and once per loaded list: it is a package manager
    // question per app, and the chip needs its size before anyone picks it.
    var launcherless by remember { mutableStateOf(emptySet<String>()) }
    var derivedFor by remember { mutableIntStateOf(-1) }
    LaunchedEffect(apps, version) {
        if (apps.isEmpty()) {
            launcherless = emptySet()
            return@LaunchedEffect
        }
        if (derivedFor == version) return@LaunchedEffect
        launcherless = withContext(Dispatchers.IO) {
            InstalledPackages.launcherless(context, apps)
        }
        derivedFor = version
    }

    val shown = remember(
        apps, query, sortOrder, filter,
        systemPackages, userPackages, disabledPackages, launcherless, pinned
    ) {
        val byFilter = when (filter) {
            ManageFilter.ALL -> apps
            ManageFilter.USER -> apps.filter { it.packageName in userPackages }
            ManageFilter.SYSTEM -> apps.filter { it.packageName in systemPackages }
            ManageFilter.DISABLED -> apps.filter { it.packageName in disabledPackages }
            ManageFilter.HIDDEN -> apps.filter { it.packageName in launcherless }
        }
        val trimmed = query.trim()
        val filtered = if (trimmed.isBlank()) {
            byFilter
        } else {
            byFilter.filter {
                appLabel(pm, it).contains(trimmed, ignoreCase = true) ||
                    it.packageName.contains(trimmed, ignoreCase = true)
            }
        }
        val order = when (sortOrder) {
            SortOrder.RECENTLY_INSTALLED -> compareByDescending<PackageInfo> { it.firstInstallTime }
            SortOrder.RECENTLY_UPDATED -> compareByDescending<PackageInfo> { it.lastUpdateTime }
            SortOrder.ALPHABETICAL -> compareBy { appLabel(pm, it).lowercase() }
        }
        filtered.sortedWith(
            compareBy<PackageInfo> { it.packageName !in pinned }.then(order)
        )
    }

    if (selecting) {
        BackHandler {
            selecting = false
            selected = emptySet()
        }
    }

    val open = openPackage
    if (open != null) {
        BackHandler { openPackage = null }
        AppDetailScreen(
            packageName = open,
            bottomPadding = bottomPadding,
            onBack = { openPackage = null },
            onChanged = { version++ }
        )
        return
    }

    val refresh: () -> Unit = {
        refreshing = true
        // The hidden set is worked out per list, so a re-read has to ask for it
        // again too: a pull is how you pick up an app installed elsewhere.
        derivedFor = -1
        refreshKey++
    }

    // Runs the reviewed batch one verified write at a time, updating progress after each
    // app and keeping every outcome for the result screen.
    fun applyBulk(request: BulkRequest) {
        if (request.targets.isEmpty()) return
        bulkState = BulkState.Running(request, 0)
        scope.launch {
            val outcomes = mutableListOf<BulkOutcome>()
            for (target in request.targets) {
                val changed = withContext(Dispatchers.IO) {
                    runCatching {
                        PackageTools.setOp(
                            context,
                            target.packageName,
                            request.choice.name,
                            request.mode
                        )
                    }.getOrDefault(false)
                }
                outcomes += BulkOutcome(target.packageName, target.label, changed)
                bulkState = BulkState.Running(request, outcomes.size)
            }
            selecting = false
            selected = emptySet()
            bulkState = BulkState.Finished(
                BulkReport(request.choice, request.mode, outcomes)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    if (selecting) {
                        stringResource(R.string.manage_selected_count, selected.size)
                    } else {
                        stringResource(R.string.tab_manage)
                    }
                )
            },
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            // Shown when this is opened from the Labs list rather than being the page: it is a
            // detail over the tabs then, with no bar underneath to go back to.
            navigationIcon = {
                if (selecting) {
                    IconButton(onClick = { selecting = false; selected = emptySet() }) {
                        Icon(
                            MiuixIcons.Close,
                            contentDescription = stringResource(R.string.manage_selection_close)
                        )
                    }
                } else if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            MiuixIcons.Back,
                            contentDescription = null
                        )
                    }
                }
            },
            actions = {
                if (selecting) {
                    // Select all is scoped to what is shown, so a search or filter is a way
                    // to pick a subset instead of having to tap every row.
                    val selectableNames = shown.mapTo(HashSet()) { it.packageName } - pinned
                    val allShownSelected = selectableNames.isNotEmpty() &&
                        selected.containsAll(selectableNames)
                    IconButton(
                        onClick = {
                            selected = if (allShownSelected) {
                                selected - selectableNames
                            } else {
                                selected + selectableNames
                            }
                        },
                        enabled = selectableNames.isNotEmpty()
                    ) {
                        Icon(
                            MiuixIcons.SelectAll,
                            contentDescription = stringResource(
                                if (allShownSelected) R.string.manage_clear_selection
                                else R.string.manage_select_all
                            )
                        )
                    }
                } else {
                    IconButton(
                        onClick = { selecting = true },
                        enabled = shown.isNotEmpty()
                    ) {
                        Icon(
                            MiuixIcons.Tasks,
                            contentDescription = stringResource(R.string.manage_select)
                        )
                    }
                    TelevisionRefreshButton(refreshing, refresh)
                    IconButton(onClick = { sortMenu = true }) {
                        Icon(MiuixIcons.Sort, contentDescription = null)
                    }
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.app_management_sort_alphabetical)) },
                            onClick = { sortOrder = SortOrder.ALPHABETICAL; sortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.app_management_sort_last_added)) },
                            onClick = { sortOrder = SortOrder.RECENTLY_INSTALLED; sortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.app_management_sort_updated)) },
                            onClick = { sortOrder = SortOrder.RECENTLY_UPDATED; sortMenu = false }
                        )
                    }
                }
            }
        )

        // Each chip gets an equal slot and shrinks its label to fit. A wide font changes the
        // text size, not the number of rows.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ManageFilter.entries.forEach { option ->
                val count = when (option) {
                    ManageFilter.ALL -> apps.size
                    ManageFilter.USER -> userPackages.size
                    ManageFilter.SYSTEM -> systemPackages.size
                    ManageFilter.DISABLED -> disabledPackages.size
                    ManageFilter.HIDDEN -> launcherless.size
                }
                AppFilterChip(
                    modifier = Modifier.weight(1f),
                    label = stringResource(
                        when (option) {
                            ManageFilter.ALL -> R.string.manage_filter_all
                            ManageFilter.USER -> R.string.manage_filter_user
                            ManageFilter.SYSTEM -> R.string.manage_filter_system
                            ManageFilter.DISABLED -> R.string.manage_filter_disabled
                            ManageFilter.HIDDEN -> R.string.manage_filter_hidden
                        }
                    ),
                    count = count,
                    selected = filter == option,
                    onClick = { filter = option }
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .televisionTextInput(MaterialTheme.shapes.extraLarge),
            placeholder = { Text(stringResource(R.string.manage_search_hint)) },
            leadingIcon = { Icon(MiuixIcons.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(MiuixIcons.Close, contentDescription = stringResource(android.R.string.cancel))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MiuixTheme.colorScheme.dividerLine,
                focusedBorderColor = MiuixTheme.colorScheme.primary
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val listState = rememberLazyListState()
            RefreshBox(
                isRefreshing = refreshing,
                onRefresh = refresh,
                modifier = Modifier.fillMaxSize()
            ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 4.dp,
                    end = 16.dp,
                    bottom = bottomPadding + if (selecting) 88.dp else 0.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.rowKey() }) { pi ->
                    val removed = pi.packageName in removedPackages

                    SegmentedCard {
                        SegmentedListItem(
                            onClick = {
                                if (selecting) {
                                    selected = if (pi.packageName in selected) {
                                        selected - pi.packageName
                                    } else {
                                        selected + pi.packageName
                                    }
                                } else {
                                    openPackage = pi.packageName
                                }
                            },
                            leadingContent = {
                                // A removed app has no icon to load the row keeps the
                                // space so the labels stay aligned with the rest.
                                if (removed) Box(modifier = Modifier.padding(20.dp)) else AppIcon(pi)
                            },
                            headlineContent = { Text(appLabel(pm, pi)) },
                            // The name has the whole first line now; the package, the chips and
                            // the arrow share the line under it, and the package gives way.
                            supportingContent = {
                                AppRowLine(pi.packageLine()) {
                                    // The same chips every app list carries, worked out in one
                                    // place: what kind of app it is, and what is worth
                                    // knowing about it.
                                    AppStatusChips(
                                        pi,
                                        hidden = pi.packageName in launcherless,
                                        removed = removed
                                    )
                                    IconButton(
                                        onClick = {
                                            val nowPinned = PinnedApps.toggle(pi.packageName)
                                            pinned = if (nowPinned) {
                                                pinned + pi.packageName
                                            } else {
                                                pinned - pi.packageName
                                            }
                                            if (nowPinned) {
                                                selected = selected - pi.packageName
                                            }
                                        }
                                    ) {
                                        Icon(
                                            MiuixIcons.Pin,
                                            contentDescription = stringResource(
                                                if (pi.packageName in pinned) R.string.manage_unpin_app
                                                else R.string.manage_pin_app
                                            ),
                                            tint = if (pi.packageName in pinned) {
                                                MiuixTheme.colorScheme.primary
                                            } else {
                                                MiuixTheme.colorScheme.onSurfaceVariantSummary
                                            }
                                        )
                                    }
                                    if (!selecting) {
                                        Icon(
                                            MiuixIcons.ChevronForward,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        )
                                    }
                                }
                            },
                            trailingContent = if (selecting) {
                                {
                                    Checkbox(
                                        checked = pi.packageName in selected,
                                        onCheckedChange = null
                                    )
                                }
                            } else {
                                null
                            },
                            // Centred, so the chip and the arrow sit against the row rather than
                            // level with its first line, and the row's own padding keeps them off
                            // the card's edge.
                            centerSlots = true
                        )
                    }
                }
            }

            // While the first read is in flight the rows are stood in for rather than left
            // as a blank page with a spinner over it; the list keeps its shape and the wait
            // is spent looking at where the content will be. A pull is not this case: the
            // rows already there stay, which is why the skeleton asks for an empty list too.
            if (loading && shown.isEmpty()) {
                CenteredMessage { LoadingIndicator() }
            }

            if (!loading && shown.isEmpty()) {
                CenteredMessage {
                    when {
                        query.isNotBlank() -> Text(
                            text = stringResource(R.string.apps_no_match),
                            style = MiuixTheme.textStyles.body2,
                            textAlign = TextAlign.Center
                        )

                        filter != ManageFilter.ALL -> Text(
                            text = stringResource(R.string.manage_filter_empty),
                            style = MiuixTheme.textStyles.body2,
                            textAlign = TextAlign.Center
                        )

                        else -> Text(
                            text = stringResource(R.string.manage_empty),
                            style = MiuixTheme.textStyles.body2,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            }


            if (selecting) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.manage_selected_count, selected.size),
                            style = MiuixTheme.textStyles.body1,
                            modifier = Modifier.weight(1f)
                        )
                        PillButton(
                            onClick = { bulkPicker = true },
                            enabled = selected.isNotEmpty()
                        ) {
                            Text(stringResource(R.string.manage_bulk_action))
                        }
                    }
                }
            }
        }
    }

    if (bulkPicker) {
        BulkOpDialog(
            count = selected.size,
            onDismiss = { bulkPicker = false },
            onApply = { choice, mode ->
                bulkPicker = false
                val targets = selected.map { packageName ->
                    val info = apps.firstOrNull { it.packageName == packageName }
                    BulkTarget(
                        packageName = packageName,
                        label = info?.let { appLabel(pm, it) } ?: packageName
                    )
                }.sortedBy { it.label.lowercase() }
                if (targets.isNotEmpty()) {
                    bulkState = BulkState.Confirm(BulkRequest(choice, mode, targets))
                }
            }
        )
    }

    when (val state = bulkState) {
        is BulkState.Confirm -> BulkConfirmDialog(
            request = state.request,
            onDismiss = { bulkState = null },
            onConfirm = { applyBulk(state.request) }
        )

        is BulkState.Running -> BulkProgressDialog(state)
        is BulkState.Finished -> BulkResultDialog(
            report = state.report,
            onDismiss = { bulkState = null }
        )

        null -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BulkOpDialog(
    count: Int,
    onDismiss: () -> Unit,
    onApply: (AppOpChoice, OpMode) -> Unit
) {
    var selectedOp by remember { mutableStateOf<AppOpChoice?>(null) }
    var selectedMode by remember { mutableStateOf<OpMode?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_bulk_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.manage_selected_count, count),
                    style = MiuixTheme.textStyles.button
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.manage_bulk_note),
                    style = MiuixTheme.textStyles.footnote1
                )
                Text(
                    text = stringResource(R.string.manage_bulk_pinned_note),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(APP_OPS, key = { it.name }) { choice ->
                        ChoiceRow(
                            selected = selectedOp?.name == choice.name,
                            label = stringResource(choice.label),
                            supporting = stringResource(choice.note),
                            onClick = { selectedOp = choice }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.manage_bulk_mode),
                    style = MiuixTheme.textStyles.button
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OpMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(opModeLabel(mode)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            PillButton(
                onClick = {
                    val choice = selectedOp
                    val mode = selectedMode
                    if (choice != null && mode != null) onApply(choice, mode)
                },
                enabled = selectedOp != null && selectedMode != null
            ) {
                Text(stringResource(R.string.manage_bulk_review))
            }
        },
        dismissButton = {
            PillButtonQuiet(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun BulkConfirmDialog(
    request: BulkRequest,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_bulk_confirm_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(
                        R.string.manage_bulk_confirm_op,
                        stringResource(request.choice.label)
                    )
                )
                Text(
                    text = stringResource(
                        R.string.manage_bulk_confirm_mode,
                        opModeLabel(request.mode)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.manage_bulk_confirm_targets,
                        request.targets.size
                    ),
                    style = MiuixTheme.textStyles.title4
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.manage_bulk_confirm_warning),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(request.targets, key = { it.packageName }) { target ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = target.label,
                                style = MiuixTheme.textStyles.body2,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = target.packageName,
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            PillButton(onClick = onConfirm) {
                Text(stringResource(R.string.manage_bulk_apply))
            }
        },
        dismissButton = {
            PillButtonQuiet(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun BulkProgressDialog(state: BulkState.Running) {
    val total = state.request.targets.size
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.manage_bulk_progress_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(
                        R.string.manage_bulk_confirm_op,
                        stringResource(state.request.choice.label)
                    ),
                    style = MiuixTheme.textStyles.footnote1
                )
                Text(
                    text = stringResource(
                        R.string.manage_bulk_confirm_mode,
                        opModeLabel(state.request.mode)
                    ),
                    style = MiuixTheme.textStyles.footnote1
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = {
                        if (total == 0) 0f else state.completed.toFloat() / total
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.manage_bulk_progress, state.completed, total))
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun BulkResultDialog(report: BulkReport, onDismiss: () -> Unit) {
    val changed = report.outcomes.count { it.changed }
    val failed = report.outcomes.size - changed
    var failuresOnly by remember(report) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_bulk_result_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.manage_bulk_result_summary, changed, failed),
                    style = MiuixTheme.textStyles.title4
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.manage_bulk_confirm_op,
                        stringResource(report.choice.label)
                    ),
                    style = MiuixTheme.textStyles.footnote1
                )
                Text(
                    text = stringResource(
                        R.string.manage_bulk_confirm_mode,
                        opModeLabel(report.mode)
                    ),
                    style = MiuixTheme.textStyles.footnote1
                )
                if (failed > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterChip(
                        selected = failuresOnly,
                        onClick = { failuresOnly = !failuresOnly },
                        label = { Text(stringResource(R.string.manage_bulk_failures_only)) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    val shown = if (failuresOnly) {
                        report.outcomes.filterNot { it.changed }
                    } else {
                        report.outcomes
                    }
                    items(shown, key = { it.packageName }) { outcome ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = outcome.label,
                                    style = MiuixTheme.textStyles.body2,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = outcome.packageName,
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = stringResource(
                                    if (outcome.changed) {
                                        R.string.manage_bulk_result_changed
                                    } else {
                                        R.string.manage_bulk_result_failed
                                    }
                                ),
                                style = MiuixTheme.textStyles.button,
                                color = if (outcome.changed) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
                if (failed > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.manage_bulk_result_failed_hint),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
        },
        confirmButton = {
            PillButton(onClick = onDismiss) {
                Text(stringResource(R.string.manage_bulk_done_action))
            }
        }
    )
}

@Composable
private fun opModeLabel(mode: OpMode): String = stringResource(
    when (mode) {
        OpMode.DEFAULT -> R.string.app_op_mode_default
        OpMode.ALLOW -> R.string.app_op_mode_allow
        OpMode.IGNORE -> R.string.app_op_mode_ignore
        OpMode.DENY -> R.string.app_op_mode_deny
    }
)
