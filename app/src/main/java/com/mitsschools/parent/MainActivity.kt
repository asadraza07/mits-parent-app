package com.mitsschools.parent

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitsschools.parent.ui.AppViewModel
import com.mitsschools.parent.ui.navigation.AppNavigation
import com.mitsschools.parent.ui.theme.MitsTheme

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Every screen has a charcoal header, so status-bar icons are always light.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            val themeMode by vm.themeMode.collectAsStateWithLifecycle()
            MitsTheme(mode = themeMode) {
                AppNavigation(vm)
            }
        }
    }
}
