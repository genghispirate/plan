package app.haven.ui.zen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/**
 * Drives "Zen Mode": after [timeoutMillis] of no interaction the interface
 * chrome fades away so only the living diorama remains; any touch instantly
 * restores it. Interruptible and cheap — a single delay coroutine re-keyed on
 * an interaction tick.
 */
class ZenModeState(
    private val timeoutMillis: Long,
    private val fadeDurationMillis: Int,
) {
    /** Bumped on every interaction; re-arms the idle timer. */
    internal var interactionTick by mutableIntStateOf(0)
        private set

    /** True once idle long enough that chrome should fade out. */
    var isZen by mutableStateOf(false)
        internal set

    val fadeMillis: Int get() = fadeDurationMillis

    fun notifyInteraction() {
        interactionTick++
        isZen = false
    }

    internal suspend fun awaitIdle() {
        delay(timeoutMillis)
        isZen = true
    }
}

/**
 * Remembers a [ZenModeState] and runs its idle timer. Default 3-second idle
 * (Section 4/5) with a gentle ~1s cross-fade — long enough to feel like the
 * world exhaling, not a hard cut.
 */
@Composable
fun rememberZenModeState(
    timeoutMillis: Long = 3_000L,
    fadeDurationMillis: Int = 1_000,
): ZenModeState {
    val state = remember(timeoutMillis, fadeDurationMillis) {
        ZenModeState(timeoutMillis, fadeDurationMillis)
    }
    LaunchedEffect(state.interactionTick) {
        state.awaitIdle()
    }
    return state
}

/**
 * The animated chrome alpha to apply to fade-able UI (interface panel, the coin
 * and growth indicators). Driven by [ZenModeState.isZen] with a timed fade —
 * a cross-fade is a content transition, so a smooth tween is appropriate here.
 */
@Composable
fun ZenModeState.chromeAlpha(): State<Float> =
    animateFloatAsState(
        targetValue = if (isZen) 0f else 1f,
        animationSpec = tween(durationMillis = fadeMillis),
        label = "zenChromeAlpha",
    )

/**
 * Any pointer contact within this modifier counts as interaction and cancels /
 * resets Zen Mode. Placed on the root so the whole screen wakes the chrome.
 */
fun Modifier.zenInteraction(state: ZenModeState): Modifier =
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent()
                state.notifyInteraction()
            }
        }
    }
