package br.edu.utfpr.roadifylogger.data.model

// Representa as configurações escolhidas pelo usuário para o nível bolha.
enum class LevelDisplayType {
    ANGLE,
    INCLINATION,
    ROOF_PITCH
}

enum class BubbleViscosity {
    LOW,
    MEDIUM,
    HIGH
}

data class LevelOptionsState(
    val showAngle: Boolean = true,
    val displayType: LevelDisplayType = LevelDisplayType.ANGLE,
    val lockLevelType: Boolean = false,
    val economyMode: Boolean = false,
    val viscosity: BubbleViscosity = BubbleViscosity.MEDIUM,
    val soundEnabled: Boolean = false
)