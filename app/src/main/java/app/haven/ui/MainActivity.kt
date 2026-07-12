package app.haven.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.haven.ui.theme.HavenTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity Compose host. Installs the system splash screen, then renders
 * the framed diorama home under [HavenTheme]. Edge-to-edge is handled inside the
 * theme's system-bar sync.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            HavenTheme {
                HavenRoot()
            }
        }
    }
}
