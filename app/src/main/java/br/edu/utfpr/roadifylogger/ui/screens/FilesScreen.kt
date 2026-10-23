package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.utfpr.roadifylogger.R
import br.edu.utfpr.roadifylogger.data.model.RecordingSession
import br.edu.utfpr.roadifylogger.ui.viewmodel.FilesViewModel
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.widget.Toast
import android.content.ActivityNotFoundException
import android.content.Context
import android.os.Build
import android.provider.DocumentsContract


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FilesScreen(
    viewModel: FilesViewModel,
    onOpenSummary: (RecordingSession) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var sessionPendingDelete by remember { mutableStateOf<RecordingSession?>(null) }
    var confirmClearAll by remember { mutableStateOf(false) }
    var sessionPendingExport by remember {
        mutableStateOf<RecordingSession?>(null)
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                sessionPendingExport?.let { session ->
                    viewModel.exportSession(session, uri)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Não foi possível acessar a pasta selecionada.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        sessionPendingExport = null
    }

    LaunchedEffect(Unit) { viewModel.refresh() }

    // Efeito para exibir o Snackbar sempre que errorMessage for preenchido
    LaunchedEffect(state.error) {
        state.error?.let { mensagem ->
            snackbarHostState.showSnackbar(
                message = mensagem,
                withDismissAction = true
            )
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.files_title)) },
                actions = {
                    if (state.sessions.isNotEmpty()) {
                        IconButton(onClick = { confirmClearAll = true }) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = stringResource(R.string.files_clear_all))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Text(
                stringResource(R.string.files_subtitle, state.sessions.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (state.sessions.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.files_empty_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.files_empty_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.sessions, key = { it.id }) { session ->
                        SessionCard(
                            session = session,
                            isMostRecent = session == state.sessions.firstOrNull(),
                            onOpenSummary = { onOpenSummary(session) },
                            onShare = { context.startActivity(viewModel.shareIntent(session)) },
                            onOpenFolder = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    viewModel.exportToDownloads(session) {
                                        abrirPastaExportada(context, session)
                                    }
                                } else {
                                    sessionPendingExport = session
                                    folderPickerLauncher.launch(null)
                                }
                            },
                            onDelete = { sessionPendingDelete = session },
                        )
                    }
                }
            }
        }
    }

    // Diálogo de Exclusão
    sessionPendingDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionPendingDelete = null },
            title = { Text(stringResource(R.string.files_delete_confirm_title)) },
            text = { Text(stringResource(R.string.files_delete_confirm_body, session.fileName)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(session)
                    sessionPendingDelete = null
                }) { Text(stringResource(R.string.files_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { sessionPendingDelete = null }) { Text(stringResource(R.string.files_cancel)) }
            },
        )
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text(stringResource(R.string.files_clear_all)) },
            text = { Text(stringResource(R.string.files_delete_confirm_body, "")) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAll()
                    confirmClearAll = false
                }) { Text(stringResource(R.string.files_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearAll = false }) { Text(stringResource(R.string.files_cancel)) }
            },
        )
    }
}

@Composable
private fun SessionCard(
    session: RecordingSession,
    isMostRecent: Boolean,
    onOpenSummary: () -> Unit,
    onShare: () -> Unit,
    onOpenFolder: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        onClick = onOpenSummary
    ) {
        Column(
            Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isMostRecent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = null,
                        tint = if (isMostRecent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    // Exibe o nome de cada arquivo gerado na coleta
                    if (session.filePaths.isNotEmpty()) {
                        session.filePaths.forEach { filePath ->
                            Text(
                                text = java.io.File(filePath).name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text(session.fileName, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            session.locationLabel ?: stringResource(R.string.files_no_location),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.Filled.SdStorage, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(formatSize(session.sizeBytes), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onOpenSummary) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = "Ver Resumo da Coleta",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.files_share))
                }
                IconButton(onClick = onOpenFolder) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = stringResource(R.string.files_open_folder)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.files_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    return if (kb < 1024) "%.1f KB".format(Locale.US, kb) else "%.1f MB".format(Locale.US, kb / 1024.0)
}

private fun abrirPastaExportada(
    context: Context,
    session: RecordingSession
) {
    val caminho = "Download/RoadifyLogger/${session.id}"

    val uri = DocumentsContract.buildDocumentUri(
        "com.android.externalstorage.documents",
        "primary:$caminho"
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(
            uri,
            DocumentsContract.Document.MIME_TYPE_DIR
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
        return
    } catch (e: Exception) {
    }

    val pacotesXiaomi = listOf(
        "com.mi.android.globalFileexplorer",
        "com.android.fileexplorer"
    )

    for (pacote in pacotesXiaomi) {
        val abrirGerenciador =
            context.packageManager.getLaunchIntentForPackage(pacote)

        if (abrirGerenciador != null) {
            try {
                context.startActivity(abrirGerenciador)

                Toast.makeText(
                    context,
                    "Pasta: $caminho",
                    Toast.LENGTH_LONG
                ).show()
                return
            } catch (e: Exception) {
            }
        }
    }

    try {
        val navegador = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        }

        context.startActivity(navegador)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Não foi possível abrir a pasta: $caminho",
            Toast.LENGTH_LONG
        ).show()
    }
}

