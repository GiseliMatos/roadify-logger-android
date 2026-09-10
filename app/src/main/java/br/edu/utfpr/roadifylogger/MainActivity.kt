package br.edu.utfpr.roadifylogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.utfpr.roadifylogger.data.AppContainer
import br.edu.utfpr.roadifylogger.data.model.SensorKind
import br.edu.utfpr.roadifylogger.ui.components.BottomBar
import br.edu.utfpr.roadifylogger.ui.components.BottomBarItem
import br.edu.utfpr.roadifylogger.ui.screens.ConfiguracoesScreen
import br.edu.utfpr.roadifylogger.ui.screens.DashboardScreen
import br.edu.utfpr.roadifylogger.ui.screens.FilesScreen
import br.edu.utfpr.roadifylogger.ui.screens.LevelScreen
import br.edu.utfpr.roadifylogger.ui.screens.SensorDetailScreen
import br.edu.utfpr.roadifylogger.ui.theme.RoadifyLoggerTheme
import br.edu.utfpr.roadifylogger.ui.viewmodel.DashboardViewModel
import br.edu.utfpr.roadifylogger.ui.viewmodel.FilesViewModel
import br.edu.utfpr.roadifylogger.ui.viewmodel.SensorDetailViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RoadifyLoggerApplication).container
        setContent {
            RoadifyLoggerTheme {
                RoadifyLoggerApp(container = container)
            }
        }
    }
}

@Composable
fun RoadifyLoggerApp(container: AppContainer) {
    var selectedItem by remember {
        mutableStateOf(BottomBarItem.SENSORES)
    }

    var showLevelScreen by rememberSaveable {
        mutableStateOf(false)
    }

    // null = mostra o dashboard; caso contrário mostra o detalhe do sensor selecionado.
    var selectedSensorDetail by remember {
        mutableStateOf<SensorKind?>(null)
    }

    Scaffold(
        // Cada tela já traz sua própria TopAppBar (Dashboard, Arquivos, Detalhe do
        // sensor). Uma TopBar aqui em cima duplicaria a barra superior nessas telas.
        bottomBar = {
            BottomBar(
                selectedItem = selectedItem,
                onItemSelected = { item ->
                    selectedItem = item
                    showLevelScreen = false
                    selectedSensorDetail = null
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            when (selectedItem) {
                BottomBarItem.CONFIGURACOES -> {
                    if (showLevelScreen) {
                        LevelScreen(
                            onBackClick = {
                                showLevelScreen = false
                            }
                        )
                    } else {
                        ConfiguracoesScreen(
                            onLevelClick = {
                                showLevelScreen = true
                            }
                        )
                    }
                }

                BottomBarItem.SENSORES -> {
                    val kindBeingViewed = selectedSensorDetail
                    if (kindBeingViewed == null) {
                        val dashboardViewModel: DashboardViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer {
                                    DashboardViewModel(
                                        recordingRepository = container.recordingRepository,
                                        settingsRepository = container.settingsRepository,
                                        locationRepository = container.locationRepository,
                                        cameraRepository = container.cameraRepository,
                                        audioRepository = container.audioRepository,
                                    )
                                }
                            },
                        )
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onOpenSensorDetail = { kind -> selectedSensorDetail = kind },
                        )
                    } else {
                        val sensorDetailViewModel: SensorDetailViewModel = viewModel(
                            key = kindBeingViewed.name,
                            factory = viewModelFactory {
                                initializer {
                                    SensorDetailViewModel(
                                        kind = kindBeingViewed,
                                        motionSensorRepository = container.motionSensorRepository,
                                    )
                                }
                            },
                        )
                        SensorDetailScreen(
                            viewModel = sensorDetailViewModel,
                            onBack = { selectedSensorDetail = null },
                        )
                    }
                }

                BottomBarItem.ARQUIVOS -> {
                    val filesViewModel: FilesViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                FilesViewModel(sessionFileRepository = container.sessionFileRepository)
                            }
                        },
                    )
                    FilesScreen(viewModel = filesViewModel)
                }
            }
        }
    }
}
