package br.edu.utfpr.roadifylogger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.utfpr.roadifylogger.ui.theme.RoadifyLoggerTheme
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import br.edu.utfpr.roadifylogger.R



enum class BottomBarItem(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    CONFIGURACOES(
        R.string.nav_settings,
        Icons.Filled.Settings
    ),
    SENSORES(
        R.string.nav_dashboard,
        Icons.Filled.Sensors
    ),
    ARQUIVOS(
        R.string.nav_files,
        Icons.Filled.Folder
    )
}

@Composable
fun BottomBar(
    selectedItem: BottomBarItem,
    onItemSelected: (BottomBarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ){
        BottomBarItem.entries.forEach { item ->
            val selected = item == selectedItem

            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppBottomBarPreview() {
    RoadifyLoggerTheme {
        BottomBar(
            selectedItem = BottomBarItem.SENSORES,
            onItemSelected = {}
        )
    }
}