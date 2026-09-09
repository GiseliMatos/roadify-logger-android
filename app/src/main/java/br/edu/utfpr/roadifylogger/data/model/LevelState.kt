package br.edu.utfpr.roadifylogger.data.model

//estado da Tela de Nível Bolha
enum class LevelOrientation {
    FLAT,
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

data class LevelState(
    val roll: Float = 0f,
    val pitch: Float = 0f,
    val orientation: LevelOrientation = LevelOrientation.FLAT,
    val isCalibrated: Boolean = false,
    val isSensorAvailable: Boolean = true
)