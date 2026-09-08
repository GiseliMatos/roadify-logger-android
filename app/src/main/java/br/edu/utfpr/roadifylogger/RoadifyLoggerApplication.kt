package br.edu.utfpr.roadifylogger

import android.app.Application
import br.edu.utfpr.roadifylogger.data.AppContainer

class RoadifyLoggerApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
