package br.edu.utfpr.roadifylogger.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.utfpr.roadifylogger.ui.theme.RoadifyLoggerTheme

@Composable
fun SensoresScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dados dos Sensores",
            style = MaterialTheme.typography.headlineSmall
        )

        ////
    }
}

@Preview(showBackground = true)
@Composable
private fun SensoresScreenPreview() {
    RoadifyLoggerTheme {
        SensoresScreen()
    }
}