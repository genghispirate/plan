package app.haven.ui.render.layers

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import app.haven.data.world.DwellingTier
import app.haven.data.world.PathSurface
import app.haven.data.world.WaterFeatureType
import app.haven.data.world.WorldState
import app.haven.ui.render.IsoProjection
import app.haven.ui.render.particle.FireflySystem
import app.haven.ui.render.particle.RainSystem
import app.haven.ui.render.particle.RippleSystem
import kotlin.math.sin

/** Resolved colors handed to the pure draw functions (no CompositionLocal in DrawScope). */
data class RenderPalette(
    val skyTop: Color,
    val skyBottom: Color,
    val ground: Color,
    val groundShade: Color,
    val water: Color,
    val waterHighlight: Color,
    val wall: Color,
    val roof: Color,
    val libraryWall: Color,
    val libraryRoof: Color,
    val trunk: Color,
    val canopy: Color,
    val firefly: Color,
    val rain: Color,
    val ripple: Color,
    val restTint: Color,
    val fog: Color,
)

fun pathColor(surface: PathSurface, p: RenderPalette): Color = when (surface) {
    PathSurface.DIRT -> Color(0xFF8A6D4B)
    PathSurface.GRAVEL -> Color(0xFF9A8C79)
    PathSurface.PAVED -> Color(0xFF9BA0A6)
    PathSurface.FLAGSTONE -> Color(0xFFB9B3A6)
}

// ---------------------------------------------------------------------------
//  SKY
// ---------------------------------------------------------------------------

fun DrawScope.drawSky(p: RenderPalette) {
    drawRect(Brush.verticalGradient(listOf(p.skyTop, p.skyBottom)))
}

// ---------------------------------------------------------------------------
//  TERRAIN — ground diamond, then paths on top.
// ---------------------------------------------------------------------------

fun DrawScope.drawTerrain(world: WorldState, proj: IsoProjection, p: RenderPalette) {
    val ground = diamondPath(proj, -5f, -5f, 8f, 8f)
    drawPath(ground, p.ground)
    drawPath(ground, p.groundShade, alpha = 0.25f)

    for (segment in world.infrastructure.paths) {
        drawLine(
            color = pathColor(segment.surface, p),
            start = proj.project(segment.from),
            end = proj.project(segment.to),
            strokeWidth = (proj.scaledTileHeight * 0.35f).coerceAtLeast(3f),
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}

private fun diamondPath(proj: IsoProjection, x: Float, y: Float, w: Float, h: Float): Path {
    val a = proj.project(coord(x, y))
    val b = proj.project(coord(x + w, y))
    val c = proj.project(coord(x + w, y + h))
    val d = proj.project(coord(x, y + h))
    return Path().apply {
        moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(d.x, d.y); close()
    }
}

private fun coord(x: Float, y: Float) = app.haven.data.world.GridCoordinate(x, y)

// ---------------------------------------------------------------------------
//  WATER — feature bodies + a continuous pixel-shifting flow overlay (Sec 4.C).
// ---------------------------------------------------------------------------

fun DrawScope.drawWater(
    world: WorldState,
    proj: IsoProjection,
    p: RenderPalette,
    elapsed: Float,
) {
    for (feature in world.water.features) {
        val center = proj.project(feature.at)
        val radius = (proj.scaledTileWidth * (0.3f + feature.volume.coerceIn(0f, 3f) * 0.25f))
            .coerceAtLeast(6f)
        // Body
        drawCircle(p.water, radius = radius, center = center)

        // Flowing highlight bands scroll across the body to fake current at ~30fps.
        if (feature.flowing || feature.type == WaterFeatureType.STREAM ||
            feature.type == WaterFeatureType.WATERFALL
        ) {
            val bands = 3
            for (i in 0 until bands) {
                val phase = elapsed * 1.4f + i * (radius / bands)
                val yOff = ((phase % (radius * 2f)) - radius)
                val bandAlpha = (1f - kotlin.math.abs(yOff) / radius).coerceIn(0f, 1f) * 0.5f
                drawLine(
                    color = p.waterHighlight.copy(alpha = bandAlpha),
                    start = Offset(center.x - radius * 0.7f, center.y + yOff),
                    end = Offset(center.x + radius * 0.7f, center.y + yOff),
                    strokeWidth = 2.5f,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
//  STRUCTURES — dwellings, library wings (roof-alpha for focus), trees.
// ---------------------------------------------------------------------------

fun DrawScope.drawStructures(
    world: WorldState,
    proj: IsoProjection,
    p: RenderPalette,
    focusedId: String?,
    roofAlpha: Float,
) {
    // Trees behind dwellings for depth.
    for (tree in world.biodiversity.trees) {
        val base = proj.project(tree.at)
        val scale = if (tree.ancient) 1.8f else 1f
        val h = proj.scaledTileHeight * 1.6f * scale
        drawLine(p.trunk, Offset(base.x, base.y), Offset(base.x, base.y - h), strokeWidth = 4f * scale)
        drawCircle(p.canopy, radius = proj.scaledTileWidth * 0.35f * scale, center = Offset(base.x, base.y - h))
    }

    for (dwelling in world.residential.dwellings) {
        val base = proj.project(dwelling.at)
        val w = proj.scaledTileWidth * dwellingScale(dwelling.tier)
        drawBuilding(base, w, w * 0.9f, p.wall, p.roof, roofAlpha = 1f)
    }

    for (wing in world.intellectualDistrict.wings) {
        val base = proj.project(wing.at)
        val w = proj.scaledTileWidth * 1.4f
        // The focused wing's roof fades to reveal the interior floor plan.
        val alpha = if (wing.id == focusedId) roofAlpha else 1f
        drawBuilding(base, w, w * 1.1f, p.libraryWall, p.libraryRoof, roofAlpha = alpha)
    }
}

private fun dwellingScale(tier: DwellingTier): Float = when (tier) {
    DwellingTier.CANVAS_TENT -> 0.8f
    DwellingTier.TIMBER_CABIN -> 1.0f
    DwellingTier.COTTAGE -> 1.15f
    DwellingTier.TOWNHOUSE -> 1.3f
    DwellingTier.MID_CENTURY_MODERN -> 1.5f
}

/** A minimal upright iso building: footprint shadow, body, and a roof cap. */
private fun DrawScope.drawBuilding(
    base: Offset,
    width: Float,
    bodyHeight: Float,
    wall: Color,
    roof: Color,
    roofAlpha: Float,
) {
    val half = width / 2f
    // Ground shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(base.x - half, base.y - half * 0.3f),
        size = Size(width, half * 0.6f),
    )
    // Body
    val bodyTop = base.y - bodyHeight
    drawRect(
        color = wall,
        topLeft = Offset(base.x - half, bodyTop),
        size = Size(width, bodyHeight),
    )
    // Roof (fades under focus)
    if (roofAlpha > 0.01f) {
        val roofPath = Path().apply {
            moveTo(base.x - half - 3f, bodyTop)
            lineTo(base.x, bodyTop - width * 0.5f)
            lineTo(base.x + half + 3f, bodyTop)
            close()
        }
        drawPath(roofPath, roof, alpha = roofAlpha)
    }
}

// ---------------------------------------------------------------------------
//  PARTICLES
// ---------------------------------------------------------------------------

fun DrawScope.drawFireflies(system: FireflySystem, p: RenderPalette) {
    for (f in system.flies) {
        drawCircle(
            color = p.firefly.copy(alpha = f.alpha),
            radius = 3.2f,
            center = Offset(f.x, f.y),
        )
        // Soft halo
        drawCircle(
            color = p.firefly.copy(alpha = f.alpha * 0.25f),
            radius = 7f,
            center = Offset(f.x, f.y),
        )
    }
}

fun DrawScope.drawRain(system: RainSystem, p: RenderPalette) {
    for (d in system.drops) {
        drawLine(
            color = p.rain.copy(alpha = 0.5f),
            start = Offset(d.x, d.y),
            end = Offset(d.x, d.y + d.length),
            strokeWidth = 2f,
        )
    }
}

fun DrawScope.drawRipples(system: RippleSystem, p: RenderPalette) {
    for (r in system.ripples) {
        drawCircle(
            color = p.ripple.copy(alpha = r.alpha * 0.6f),
            radius = r.radius,
            center = Offset(r.x, r.y),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
        )
    }
}

// ---------------------------------------------------------------------------
//  ATMOSPHERE — rest tint, fog vignette, standby sky clock.
// ---------------------------------------------------------------------------

fun DrawScope.drawRestTint(intensity: Float, p: RenderPalette) {
    if (intensity <= 0f) return
    drawRect(p.restTint.copy(alpha = 0.35f * intensity))
}

fun DrawScope.drawFog(density: Float, p: RenderPalette) {
    if (density <= 0f) return
    // Edge vignette that recedes as sleep consistency clears the map.
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, p.fog.copy(alpha = density.coerceIn(0f, 1f))),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.maxDimension * 0.75f,
        ),
    )
}

fun DrawScope.drawSkyClock(timeText: String, color: Color) {
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(), (color.red * 255).toInt(),
            (color.green * 255).toInt(), (color.blue * 255).toInt(),
        )
        textSize = size.minDimension * 0.14f
        typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(
        timeText,
        size.width / 2f,
        size.height * 0.28f,
        paint,
    )
}

/** Convenience for particle bounds. */
fun DrawScope.fullBounds(): Rect = Rect(0f, 0f, size.width, size.height)
