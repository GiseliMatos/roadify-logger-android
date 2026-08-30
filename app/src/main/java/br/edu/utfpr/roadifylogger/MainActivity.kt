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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.edu.utfpr.roadifylogger.ui.components.BottomBar
import br.edu.utfpr.roadifylogger.ui.components.TopBar
import br.edu.utfpr.roadifylogger.ui.components.BottomBarItem
import br.edu.utfpr.roadifylogger.ui.screens.ArquivosScreen
import br.edu.utfpr.roadifylogger.ui.screens.ConfiguracoesScreen
import br.edu.utfpr.roadifylogger.ui.screens.SensoresScreen
import br.edu.utfpr.roadifylogger.ui.theme.RoadifyLoggerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoadifyLoggerTheme {
                RoadifyLoggerApp()
            }
        }
    }
}

@Composable
fun RoadifyLoggerApp() {
    var selectedItem by remember { mutableStateOf(BottomBarItem.SENSORES) }

    Scaffold(
        topBar = {
            TopBar(title = "Roadify Logger")
        },
        bottomBar = {
            BottomBar(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedItem) {
                BottomBarItem.CONFIGURACOES -> ConfiguracoesScreen()
                BottomBarItem.SENSORES -> SensoresScreen()
                BottomBarItem.ARQUIVOS -> ArquivosScreen()
            }
        }
    }
}