package app.haven.ui.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.dp
import app.haven.data.world.DayPhase
import app.haven.data.world.WorldState
import app.haven.ui.render.layers.RenderPalette
import app.haven.ui.render.layers.drawFireflies
import app.haven.ui.render.layers.drawFog
import app.haven.ui.render.layers.drawRain
import app.haven.ui.render.layers.drawRestTint
import app.haven.ui.render.layers.drawRipples
import app.haven.ui.render.layers.drawSky
import app.haven.ui.render.layers.drawSkyClock
import app.haven.ui.render.layers.drawStructures
import app.haven.ui.render.layers.drawTerrain
import app.haven.ui.render.layers.drawWater
import app.haven.ui.theme.Haven
import app.haven.ui.theme.HavenExtendedColors
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The Compose Canvas layer controller — the render system's heart.
 *
 * A single [Canvas] composites, back-to-front: sky, terrain + paths, water
 * (with the flowing shader), structures, particle systems, then the rest-tint,
 * fog vignette and (in standby) the sky clock. A [withFrameNanos] loop steps
 * the particle engines by real delta-time and bumps the render tick so the
 * Canvas redraws every frame. Tapping a structure runs the spring camera focus
 * (Section 4.B); tapping again zooms back out.
 */
@Composable
fun DioramaCanvas(
    world: WorldState,
    standby: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val extended = Haven.colors
    val palette = remember(extended, world.dayPhase) { buildPalette(extended, world.dayPhase) }

    val render = rememberDioramaRenderState()
    val camera = remember { DioramaCamera() }
    val scope = rememberCoroutineScope()
    val currentWorld by rememberUpdatedState(world)

    var sizePx by remember { mutableStateOf(Size.Zero) }
    val tileW = with(density) { 64.dp.toPx() }
    val tileH = with(density) { 32.dp.toPx() }

    // Per-frame render loop: delta-time step of the particle systems.
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L && sizePx != Size.Zero) {
                    val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    render.step(
                        deltaSeconds = dt,
                        world = currentWorld,
                        bounds = Rect(0f, 0f, sizePx.width, sizePx.height),
                        groundY = sizePx.height * 0.82f,
                    )
                }
                last = now
            }
        }
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { sizePx = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(Unit) {
                detectTapGestures {
                    if (camera.focusedId != null) {
                        scope.launch { camera.reset() }
                    } else {
                        currentWorld.intellectualDistrict.wings.firstOrNull()?.let { wing ->
                            val iso = IsoProjection.isoOf(wing.at, tileW, tileH)
                            scope.launch { camera.focusOn(wing.id, -iso) }
                        }
                    }
                }
            },
    ) {
        // Subscribe to the render tick so this draw lambda re-runs each frame.
        render.frame.let { }

        val origin = Offset(size.width / 2f, size.height * 0.42f)
        val proj = IsoProjection(
            tileWidth = tileW,
            tileHeight = tileH,
            origin = origin,
            cameraOffset = camera.offset.value,
            zoom = camera.zoom.value,
        )

        drawSky(palette)
        drawTerrain(currentWorld, proj, palette)
        drawWater(currentWorld, proj, palette, render.elapsedSeconds)
        drawStructures(currentWorld, proj, palette, camera.focusedId, camera.roofAlpha.value)
        drawFireflies(render.fireflies, palette)
        drawRain(render.rain, palette)
        drawRipples(render.ripples, palette)
        drawRestTint(currentWorld.restState.intensity, palette)
        drawFog(currentWorld.atmosphere.fog.density, palette)
        // The draw lambda already runs each frame; formatting the clock here
        // keeps the per-frame tick out of composition (no per-frame recompose).
        if (standby) drawSkyClock(LocalTime.now().format(CLOCK_FORMAT), CLOCK_COLOR)
    }
}

private val CLOCK_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm")
private val CLOCK_COLOR = Color(0xF2FFFFFF)

/** Resolves the environmental palette for a circadian phase. */
private fun buildPalette(extended: HavenExtendedColors, phase: DayPhase): RenderPalette {
    val (skyTop, skyBottom) = skyColorsFor(phase, extended)
    return RenderPalette(
        skyTop = skyTop,
        skyBottom = skyBottom,
        ground = Color(0xFF6E8B5A),
        groundShade = Color(0xFF000000),
        water = extended.water,
        waterHighlight = Color(0xFFBFE6F2),
        wall = Color(0xFFD8C5A8),
        roof = Color(0xFF7A4A32),
        libraryWall = Color(0xFFE8DFC8),
        libraryRoof = extended.frameOuter,
        trunk = Color(0xFF5A3B24),
        canopy = Color(0xFF4F7A52),
        firefly = extended.firefly,
        rain = Color(0xFFBFC9D6),
        ripple = Color(0xFFDDEAF2),
        restTint = extended.restOvercast,
        fog = extended.restOvercast,
    )
}

private fun skyColorsFor(phase: DayPhase, extended: HavenExtendedColors): Pair<Color, Color> =
    when (phase) {
        DayPhase.SUNRISE -> Color(0xFFF7C59F) to Color(0xFFFCE8C8)
        DayPhase.GOLDEN_HOUR -> Color(0xFFF4A259) to Color(0xFFF6E1B0)
        DayPhase.DAYLIGHT -> extended.skyTop to extended.skyBottom
        DayPhase.SUNSET -> Color(0xFFE76F51) to Color(0xFFF4A261)
        DayPhase.DUSK -> Color(0xFF3D405B) to Color(0xFFE07A5F)
        DayPhase.MIDNIGHT -> Color(0xFF10132A) to Color(0xFF23294A)
    }
