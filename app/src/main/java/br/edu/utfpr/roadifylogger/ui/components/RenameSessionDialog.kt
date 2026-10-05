package br.edu.utfpr.roadifylogger.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.utfpr.roadifylogger.R

@Composable
fun RenameSessionDialog(
    initialName: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newNameText by remember(initialName) {
        mutableStateOf(initialName.removeSuffix(".csv"))
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.rename_dialog_title)) },
        text = {
            OutlinedTextField(
                value = newNameText,
                onValueChange = { newNameText = it },
                label = { Text(stringResource(id = R.string.rename_dialog_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newNameText.isNotBlank()) {
                        onConfirm(newNameText)
                    }
                    onDismissRequest()
                }
            ) {
                Text(stringResource(id = R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.action_cancel))
            }
        }
    )
}
