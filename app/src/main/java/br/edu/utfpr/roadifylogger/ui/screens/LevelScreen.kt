package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.utfpr.roadifylogger.ui.components.BubbleLevel
import br.edu.utfpr.roadifylogger.ui.viewmodel.LevelViewModel
import java.util.Locale
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton

// Apresenta o nível, os valores de Roll e Pitch e a opção de calibração.
@Composable
fun LevelScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    levelViewModel: LevelViewModel = viewModel()
){
    val state by levelViewModel.state.collectAsState()

    DisposableEffect(levelViewModel) {
        levelViewModel.startListening()

        onDispose {
            levelViewModel.stopListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        }

        Text(
            text = "Calibração de Montagem",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Ajuste a posição do smartphone no veículo até que a bolha esteja perfeitamente centrada para garantir dados precisos dos sensores.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        if (state.isSensorAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    BubbleLevel(
                        roll = state.roll,
                        pitch = state.pitch,
                        modifier = Modifier.size(280.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LevelValueCard(
                            label = "EIXO X (ROLL)",
                            value = state.roll,
                            modifier = Modifier.weight(1f)
                        )

                        LevelValueCard(
                            label = "EIXO Y (PITCH)",
                            value = state.pitch,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = levelViewModel::calibrate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null
                        )

                        Text(
                            text = "Calibrar",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        } else {
            Text(
                text = "O acelerômetro não está disponível neste dispositivo.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LevelValueCard(
    label: String,
    value: Float,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = String.format(
                    Locale.getDefault(),
                    "%.1f°",
                    value
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}