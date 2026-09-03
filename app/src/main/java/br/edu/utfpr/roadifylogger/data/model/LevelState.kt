package br.edu.utfpr.roadifylogger.data.model

//estado da Tela de Nível Bolha
data class LevelState(
    val roll: Float = 0f,
    val pitch: Float = 0f,
    val isCalibrated: Boolean = false,
    val isSensorAvailable: Boolean = true
)