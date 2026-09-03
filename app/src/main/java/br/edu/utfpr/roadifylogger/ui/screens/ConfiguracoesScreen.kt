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

@Composable
fun ConfiguracoesScreen(
    modifier: Modifier = Modifier,
    viewModel: ConfiguracoesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ConfiguracoesScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun ConfiguracoesScreen(
    state: ConfiguracoesState,
    onEvent: (ConfiguracoesEvent) -> Unit,
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
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = state.marcaSmartphone,
            onValueChange = { onEvent(ConfiguracoesEvent.MarcaSmartphoneChanged(it)) },
            label = { Text("Marca do Smartphone") },
            modifier = Modifier.fillMaxWidth()
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
                        selected = state.posicaoTelefone == posicao,
                        onClick = { onEvent(ConfiguracoesEvent.PosicaoTelefoneChanged(posicao)) }
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
            value = state.taxaGpsMs,
            onValueChange = { onEvent(ConfiguracoesEvent.TaxaGpsChanged(it)) },
            label = { Text("Taxa de atualização do GPS (ms)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.taxaSensoresHz,
            onValueChange = { onEvent(ConfiguracoesEvent.TaxaSensoresChanged(it)) },
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
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfiguracoesScreenPreview() {
    RoadifyLoggerTheme {
        ConfiguracoesScreen(
            state = ConfiguracoesState(),
            onEvent = {}
        )
    }
}