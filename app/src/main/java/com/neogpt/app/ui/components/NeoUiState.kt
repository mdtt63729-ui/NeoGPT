package com.neogpt.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.neogpt.app.settings.AppSettings
import com.neogpt.app.settings.rememberAppSettingsState

@Composable
fun neoUiSettings(): AppSettings.State = rememberAppSettingsState(LocalContext.current)
