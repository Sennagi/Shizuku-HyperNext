package moe.shizuku.manager.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import moe.shizuku.manager.ui.miuix.Icon
import moe.shizuku.manager.ui.miuix.IconButton
import moe.shizuku.manager.ui.miuix.LinearProgressIndicator
import moe.shizuku.manager.ui.miuix.LocalTopAppBarScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import moe.shizuku.manager.R
import moe.shizuku.manager.ui.theme.LocalTelevision

/**
 * A list that can be read again: pulled down on a phone, a button on a TV.
 *
 * Pulling is a finger's gesture, and a remote has nothing like it. Walking the focus down a list
 * scrolls it from the D-pad, and the pull indicator answered that scroll - it came down over the
 * top of the list on its way past without anyone asking for a refresh. On a TV the list is a plain
 * box, a thin bar along its top says a read is under way, and [TelevisionRefreshButton] in the
 * screen's bar is what asks for one. On a phone this is MIUIX [PullToRefresh].
 */
@Composable
fun RefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.() -> Unit
) {
    if (!LocalTelevision.current) {
        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
            contentPadding = contentPadding,
            topAppBarScrollBehavior = LocalTopAppBarScrollBehavior.current,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
        return
    }
    Box(modifier = modifier) {
        content()
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * The refresh a [RefreshBox] has on a TV in place of the pull, for the screen's top bar. Off a TV
 * it is nothing: the pull is how a phone asks.
 */
@Composable
fun TelevisionRefreshButton(isRefreshing: Boolean, onRefresh: () -> Unit) {
    if (!LocalTelevision.current) return
    // Never disabled, only deaf while a read is under way: a disabled button gives up focus, and
    // pressing refresh sent the focus off the page and back to the rail.
    IconButton(onClick = { if (!isRefreshing) onRefresh() }) {
        Icon(MiuixIcons.Refresh, contentDescription = stringResource(R.string.device_refresh))
    }
}
