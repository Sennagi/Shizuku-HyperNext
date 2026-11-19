package moe.shizuku.manager.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import androidx.compose.material.icons.rounded.Stop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import moe.shizuku.manager.ui.miuix.FilledIconButton
import moe.shizuku.manager.ui.miuix.Icon
import androidx.compose.material3.IconButtonDefaults
import moe.shizuku.manager.ui.miuix.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import moe.shizuku.manager.ui.miuix.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import moe.shizuku.manager.R
import moe.shizuku.manager.authorization.ClientProcessInfo

/** One attached client process under an app in the Apps list. */
@Composable
fun ClientProcessRow(process: ClientProcessInfo, onStop: () -> Unit) {
    ListItem(
        modifier = Modifier.padding(start = 16.dp),
        headlineContent = {
            Text(stringResource(R.string.apps_process_pid, process.pid))
        },
        trailingContent = {
            FilledIconButton(
                onClick = onStop,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MiuixTheme.colorScheme.error,
                    contentColor = MiuixTheme.colorScheme.onError
                )
            ) {
                Icon(
                    MiuixIcons.Close,
                    contentDescription = stringResource(R.string.apps_process_stop)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
