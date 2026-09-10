package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.utfpr.roadifylogger.data.model.BubbleViscosity
import br.edu.utfpr.roadifylogger.data.model.LevelDisplayType
import br.edu.utfpr.roadifylogger.ui.viewmodel.LevelOptionsViewModel

// Apresenta e permite alterar as configurações da Tela de Nível Bolha.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelOptionsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LevelOptionsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    var showDisplayTypeDialog by remember {
        mutableStateOf(false)
    }

    var showViscosityDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Opções")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SwitchOptionRow(
                    title = "Mostrar o valor do ângulo",
                    description = "Mostrar os valores na tela",
                    checked = state.showAngle,
                    onCheckedChange = viewModel::setShowAngle
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                SelectionOptionRow(
                    title = "Tipo de informação",
                    selectedValue = displayTypeLabel(
                        state.displayType
                    ),
                    enabled = state.showAngle,
                    onClick = {
                        showDisplayTypeDialog = true
                    }
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                SwitchOptionRow(
                    title = "Bloqueio",
                    description = "Bloquear o tipo de nível utilizado",
                    checked = state.lockLevelType,
                    onCheckedChange =
                        viewModel::setLockLevelType
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                SwitchOptionRow(
                    title = "Modo econômico",
                    description = "Reduzir a qualidade das animações para economizar bateria",
                    checked = state.economyMode,
                    onCheckedChange = viewModel::setEconomyMode
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                SelectionOptionRow(
                    title = "Viscosidade",
                    selectedValue = viscosityLabel(
                        state.viscosity
                    ),
                    enabled = !state.economyMode,
                    onClick = {
                        showViscosityDialog = true
                    }
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                SwitchOptionRow(
                    title = "Efeitos de som",
                    description = "Reproduzir som quando a medição estiver estabilizada",
                    checked = state.soundEnabled,
                    onCheckedChange =
                        viewModel::setSoundEnabled
                )
            }
        }
    }

    if (showDisplayTypeDialog) {
        AlertDialog(
            onDismissRequest = {
                showDisplayTypeDialog = false
            },
            title = {
                Text(text = "Tipo de informação")
            },
            text = {
                Column {
                    LevelDisplayType.entries.forEach { type ->
                        ListItem(
                            headlineContent = {
                                Text(displayTypeLabel(type))
                            },
                            leadingContent = {
                                RadioButton(
                                    selected =
                                        state.displayType == type,
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.setDisplayType(type)
                                showDisplayTypeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDisplayTypeDialog = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    if (showViscosityDialog) {
        AlertDialog(
            onDismissRequest = {
                showViscosityDialog = false
            },
            title = {
                Text(text = "Viscosidade")
            },
            text = {
                Column {
                    BubbleViscosity.entries.forEach { viscosity ->
                        ListItem(
                            headlineContent = {
                                Text(viscosityLabel(viscosity))
                            },
                            leadingContent = {
                                RadioButton(
                                    selected =
                                        state.viscosity == viscosity,
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.setViscosity(viscosity)
                                showViscosityDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showViscosityDialog = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SwitchOptionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(text = description)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier.clickable {
            onCheckedChange(!checked)
        }
    )
}

@Composable
private fun SelectionOptionRow(
    title: String,
    selectedValue: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(text = selectedValue)
        },
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
    )
}

private fun displayTypeLabel(
    type: LevelDisplayType
): String {
    return when (type) {
        LevelDisplayType.ANGLE ->
            "Ângulo em graus"

        LevelDisplayType.INCLINATION ->
            "Inclinação em porcentagem"

        LevelDisplayType.ROOF_PITCH ->
            "Inclinação do telhado"
    }
}

private fun viscosityLabel(
    viscosity: BubbleViscosity
): String {
    return when (viscosity) {
        BubbleViscosity.LOW ->
            "Baixa — a bolha move-se mais rápido"

        BubbleViscosity.MEDIUM ->
            "Média — a bolha move-se normalmente"

        BubbleViscosity.HIGH ->
            "Alta — a bolha move-se mais lentamente"
    }
}