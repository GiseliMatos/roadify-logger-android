package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.utfpr.roadifylogger.data.model.Posicao
import br.edu.utfpr.roadifylogger.ui.theme.RoadifyLoggerTheme
import br.edu.utfpr.roadifylogger.ui.viewmodel.ConfiguracoesEvent
import br.edu.utfpr.roadifylogger.ui.viewmodel.ConfiguracoesState
import br.edu.utfpr.roadifylogger.ui.viewmodel.ConfiguracoesViewModel

// Wrapper com estado
@Composable
fun ConfiguracoesScreen(
    onLevelClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfiguracoesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ConfiguracoesScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
        onLevelClick = onLevelClick,
        modifier = modifier
    )
}

// Apresenta as opções de configuração disponíveis no aplicativo.
@Composable
fun ConfiguracoesScreen(
    state: ConfiguracoesState,
    onEvent: (ConfiguracoesEvent) -> Unit,
    onLevelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = state.modeloSmartphone,
            onValueChange = { onEvent(ConfiguracoesEvent.ModeloSmartphoneChanged(it)) },
            label = { Text("Modelo do Smartphone") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Posição do Telefone",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Posicao.entries.forEach { posicao ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = state.posicaoTelefone == posicao.name,
                        onClick = { onEvent(ConfiguracoesEvent.PosicaoTelefoneChanged(posicao.name)) }
                    )
                    Text(text = posicao.descricao)
                }
            }
        }

        OutlinedTextField(
            value = state.marcaVeiculo,
            onValueChange = { onEvent(ConfiguracoesEvent.MarcaVeiculoChanged(it)) },
            label = { Text("Marca do Veículo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.modeloVeiculo,
            onValueChange = { onEvent(ConfiguracoesEvent.ModeloVeiculoChanged(it)) },
            label = { Text("Modelo do Veículo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.quilometragemVeiculo,
            onValueChange = { onEvent(ConfiguracoesEvent.QuilometragemVeiculoChanged(it)) },
            label = { Text("Quilometragem") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = if (state.taxaGpsMs == 0) "" else state.taxaGpsMs.toString(),
            onValueChange = {
                val intVal = it.toIntOrNull() ?: 0
                onEvent(ConfiguracoesEvent.TaxaGpsChanged(intVal))
            },
            label = { Text("Taxa de atualização do GPS (ms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = if (state.taxaSensoresHz == 0) "" else state.taxaSensoresHz.toString(),
            onValueChange = {
                val intVal = it.toIntOrNull() ?: 0
                onEvent(ConfiguracoesEvent.TaxaSensoresChanged(intVal))
            },
            label = { Text("Taxa de atualização dos sensores (Hz)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onEvent(ConfiguracoesEvent.Salvar) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Configurações")
        }

        Button(
            onClick = onLevelClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Calibração de montagem",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfiguracoesScreenPreview() {
    RoadifyLoggerTheme {
        ConfiguracoesScreen(
            state = ConfiguracoesState(),
            onEvent = {},
            onLevelClick = {}
        )
    }
}