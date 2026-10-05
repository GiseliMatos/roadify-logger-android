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
        title = { Text("Renomear Coleta") },
        text = {
            OutlinedTextField(
                value = newNameText,
                onValueChange = { newNameText = it },
                label = { Text("Nome do arquivo") },
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
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}