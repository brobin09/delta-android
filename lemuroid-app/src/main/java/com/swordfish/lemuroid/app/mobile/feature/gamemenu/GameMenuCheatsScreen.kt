package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.cheats.Cheat
import com.swordfish.lemuroid.app.shared.cheats.CheatsStore

@Composable
fun GameMenuCheatsScreen(gameMenuRequest: GameMenuActivity.GameMenuRequest) {
    val context = LocalContext.current
    val store = remember { CheatsStore(context) }
    val gameId = gameMenuRequest.game.id
    val cheats = remember { mutableStateListOf<Cheat>().apply { addAll(store.load(gameId)) } }
    var showAddDialog by remember { mutableStateOf(false) }

    fun persist() = store.save(gameId, cheats.toList())

    Column {
        ListItem(
            headlineContent = { Text(stringResource(R.string.game_menu_cheats_add)) },
            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
            modifier = Modifier.clickable { showAddDialog = true },
        )

        if (cheats.isEmpty()) {
            Text(
                text = stringResource(R.string.game_menu_cheats_empty),
                modifier = Modifier.padding(16.dp),
            )
        }

        LazyColumn {
            itemsIndexed(cheats) { index, cheat ->
                ListItem(
                    headlineContent = { Text(cheat.name) },
                    supportingContent = { Text(cheat.code) },
                    leadingContent = {
                        IconButton(onClick = {
                            cheats.removeAt(index)
                            persist()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.game_menu_cheats_delete))
                        }
                    },
                    trailingContent = {
                        Switch(
                            checked = cheat.enabled,
                            onCheckedChange = {
                                cheats[index] = cheat.copy(enabled = it)
                                persist()
                            },
                        )
                    },
                )
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.game_menu_cheats_add)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.game_menu_cheats_name)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text(stringResource(R.string.game_menu_cheats_code)) },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = code.isNotBlank(),
                    onClick = {
                        cheats.add(Cheat(name.ifBlank { code.trim() }, code.trim(), true))
                        persist()
                        showAddDialog = false
                    },
                ) { Text(stringResource(R.string.game_menu_cheats_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}
