package com.neogpt.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NeoGptApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization
        // Dependency graph initialized by Hilt
        // Logging, configuration loading happen here
    }
}
