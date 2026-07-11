package app.haven.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.haven.ui.home.HomeScreen
import app.haven.ui.onboarding.OnboardingScreen
import app.haven.ui.onboarding.OnboardingViewModel

/**
 * Top-level gate. While a founding onboarding choice is due it takes over the
 * screen (no skip); otherwise the home diorama shows. The transition between
 * the two crossfades rather than hard-cutting, in keeping with Section 4.
 */
@Composable
fun HavenRoot() {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Stable key for the crossfade: which surface is on screen.
    val target = when {
        state == null -> RootSurface.LOADING
        state?.step != null -> RootSurface.ONBOARDING
        else -> RootSurface.HOME
    }

    Crossfade(targetState = target, animationSpec = tween(400), label = "rootSurface") { surface ->
        when (surface) {
            RootSurface.LOADING ->
                androidx.compose.foundation.layout.Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                )

            RootSurface.ONBOARDING -> {
                val current = state
                val step = current?.step
                if (step != null) {
                    OnboardingScreen(
                        step = step,
                        completedCount = current.completedCount,
                        onCommit = viewModel::commit,
                    )
                }
            }

            RootSurface.HOME -> HomeScreen()
        }
    }
}

private enum class RootSurface { LOADING, ONBOARDING, HOME }
