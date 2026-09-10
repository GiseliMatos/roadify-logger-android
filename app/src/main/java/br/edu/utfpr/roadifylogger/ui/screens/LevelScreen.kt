package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.utfpr.roadifylogger.data.model.LevelDisplayType
import br.edu.utfpr.roadifylogger.data.model.LevelOrientation
import br.edu.utfpr.roadifylogger.ui.components.BubbleLevel
import br.edu.utfpr.roadifylogger.ui.components.LinearBubbleLevel
import br.edu.utfpr.roadifylogger.ui.viewmodel.LevelOptionsViewModel
import br.edu.utfpr.roadifylogger.ui.viewmodel.LevelViewModel
import kotlin.math.tan
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

// Apresenta o nível, os valores de Roll e Pitch e as opções de calibração.
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LevelScreen(
    onBackClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier,
    levelViewModel: LevelViewModel = viewModel(),
    optionsViewModel: LevelOptionsViewModel = viewModel()
) {
    val state by levelViewModel.state.collectAsState()
    val optionsState by optionsViewModel.state.collectAsState()

    var lockedOrientation by remember {
        mutableStateOf<LevelOrientation?>(null)
    }

    LaunchedEffect(optionsState.lockLevelType) {
        if (!optionsState.lockLevelType) {
            lockedOrientation = null
        }
    }

    val displayedOrientation =
        lockedOrientation ?: state.orientation

    val toneGenerator = remember {
        ToneGenerator(
            AudioManager.STREAM_NOTIFICATION,
            70
        )
    }

    DisposableEffect(levelViewModel) {
        levelViewModel.startListening()

        onDispose {
            levelViewModel.stopListening()
        }
    }

    DisposableEffect(toneGenerator) {
        onDispose {
            toneGenerator.release()
        }
    }

    val currentState by rememberUpdatedState(state)
    val currentOrientation by rememberUpdatedState(displayedOrientation)
    val soundEnabled by rememberUpdatedState(
        optionsState.soundEnabled
    )

    LaunchedEffect(toneGenerator) {
        while (isActive) {
            delay(500)

            if (soundEnabled) {
                val isLevel = when (currentOrientation) {
                    LevelOrientation.FLAT -> {
                        abs(currentState.roll) <= 1f &&
                                abs(currentState.pitch) <= 1f
                    }

                    LevelOrientation.TOP,
                    LevelOrientation.BOTTOM -> {
                        abs(currentState.roll) <= 1f
                    }

                    LevelOrientation.LEFT,
                    LevelOrientation.RIGHT -> {
                        abs(currentState.pitch) <= 1f
                    }
                }

                if (isLevel) {
                    toneGenerator.startTone(
                        ToneGenerator.TONE_PROP_BEEP,
                        100
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Calibração de montagem")
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ajuste a posição do smartphone no veículo até que a bolha esteja perfeitamente centrada.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (optionsState.lockLevelType) {
                OutlinedButton(
                    onClick = {
                        lockedOrientation =
                            if (lockedOrientation == null) {
                                state.orientation
                            } else {
                                null
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector =
                            if (lockedOrientation == null) {
                                Icons.Filled.LockOpen
                            } else {
                                Icons.Filled.Lock
                            },
                        contentDescription = null
                    )

                    Text(
                        text =
                            if (lockedOrientation == null) {
                                "Bloquear tipo de nível"
                            } else {
                                "Desbloquear tipo de nível"
                            },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (state.isSensorAvailable) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(24.dp)
                    ) {
                        when (displayedOrientation) {
                            LevelOrientation.FLAT -> {
                                BubbleLevel(
                                    roll = state.roll,
                                    pitch = state.pitch,
                                    modifier = Modifier.size(280.dp),
                                    viscosity = optionsState.viscosity,
                                    economyMode = optionsState.economyMode
                                    )

                                if (optionsState.showAngle) {
                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(16.dp)
                                    ) {
                                        LevelValueCard(
                                            label = "EIXO X (ROLL)",
                                            value = state.roll,
                                            displayType =
                                                optionsState.displayType,
                                            modifier = Modifier.weight(1f)
                                        )

                                        LevelValueCard(
                                            label = "EIXO Y (PITCH)",
                                            value = state.pitch,
                                            displayType =
                                                optionsState.displayType,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            LevelOrientation.TOP,
                            LevelOrientation.BOTTOM -> {
                                LinearBubbleLevel(
                                    value = state.roll,
                                    modifier = Modifier.fillMaxWidth(),
                                    viscosity = optionsState.viscosity,
                                    economyMode = optionsState.economyMode,
                                    )

                                if (optionsState.showAngle) {
                                    LevelValueCard(
                                        label = "EIXO X (ROLL)",
                                        value = state.roll,
                                        displayType =
                                            optionsState.displayType,
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            LevelOrientation.LEFT,
                            LevelOrientation.RIGHT -> {
                                Box(
                                    modifier = Modifier.size(280.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LinearBubbleLevel(
                                        value = state.pitch,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .rotate(90f),
                                        viscosity = optionsState.viscosity,
                                        economyMode = optionsState.economyMode,
                                        )
                                }

                                if (optionsState.showAngle) {
                                    LevelValueCard(
                                        label = "EIXO Y (PITCH)",
                                        value = state.pitch,
                                        displayType =
                                            optionsState.displayType,
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    )
                                }
                            }
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

                        Button(
                            onClick = onOptionsClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = null
                            )

                            Text(
                                text = "Opções",
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
}

@Composable
private fun LevelValueCard(
    label: String,
    value: Float,
    displayType: LevelDisplayType,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]

    val formattedValue = when (displayType) {
        LevelDisplayType.ANGLE -> {
            String.format(
                locale,
                "%.1f°",
                value.coerceIn(-99.9f, 99.9f)
            )
        }

        LevelDisplayType.INCLINATION -> {
            val inclination =
                (100f * value / 45f)
                    .coerceIn(-999.9f, 999.9f)

            String.format(
                locale,
                "%.1f%%",
                inclination
            )
        }

        LevelDisplayType.ROOF_PITCH -> {
            val roofPitch =
                (12.0 * tan(Math.toRadians(value.toDouble())))
                    .coerceIn(-99.999, 99.999)

            String.format(
                locale,
                "%.3f",
                roofPitch
            )
        }
    }

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
                text = formattedValue,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}