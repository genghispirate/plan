package app.haven.ui.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import app.haven.data.world.DayPhase
import app.haven.data.world.WeatherPhase
import app.haven.data.world.WorldState
import app.haven.ui.render.particle.FireflySystem
import app.haven.ui.render.particle.RainSystem
import app.haven.ui.render.particle.RippleSystem

/**
 * Owns the diorama's live animation state: the three particle systems, an
 * ever-advancing [elapsedSeconds] clock that drives the water flow shader, and
 * a [frame] tick that the Canvas reads to force a redraw each frame.
 *
 * Stepped once per display frame by the render loop in [DioramaCanvas]; the
 * layer draw functions only ever read from it.
 */
class DioramaRenderState {
    val fireflies = FireflySystem()
    val rain = RainSystem()
    val ripples = RippleSystem()

    /** Monotonic seconds since first frame; feeds the flowing-water phase. */
    var elapsedSeconds by mutableFloatStateOf(0f)
        private set

    /** Redraw trigger — reading this in a Canvas subscribes it to each step. */
    var frame by mutableIntStateOf(0)
        private set

    fun step(deltaSeconds: Float, world: WorldState, bounds: Rect, groundY: Float) {
        elapsedSeconds += deltaSeconds

        val isNight = world.dayPhase == DayPhase.MIDNIGHT || world.dayPhase == DayPhase.DUSK
        fireflies.update(
            deltaSeconds = deltaSeconds,
            active = world.biodiversity.fireflies.active && isNight,
            density = world.biodiversity.fireflies.density,
            bounds = bounds,
        )

        val rainIntensity = when (world.weather) {
            WeatherPhase.RAINY -> world.restState.intensity.coerceIn(0.5f, 1f)
            WeatherPhase.OVERCAST -> 0f
            WeatherPhase.SUNSHINE -> 0f
        }
        rain.update(deltaSeconds, rainIntensity, bounds, groundY) { x ->
            ripples.spawn(x, groundY)
        }
        ripples.update(deltaSeconds)

        frame++
    }
}

@Composable
fun rememberDioramaRenderState(): DioramaRenderState = remember { DioramaRenderState() }
