package app.haven.ui.render.particle

import androidx.compose.ui.geometry.Rect
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight, allocation-conscious particle engines for the diorama's
 * atmospheric systems (Section 4.C). Each holds mutable particle state and is
 * stepped by delta-time from the render loop; drawing is done separately by the
 * layer functions that read these lists.
 */

// ---------------------------------------------------------------------------
//  FIREFLIES — nocturnal float-and-fade yellow vectors (meditation reward).
// ---------------------------------------------------------------------------

class FireflySystem(private val rng: Random = Random(0xF17E)) {
    class Firefly(
        var x: Float,
        var y: Float,
        var phase: Float,
        val driftSpeed: Float,
        val blinkSpeed: Float,
    ) {
        /** 0f..1f glow, a slow sine so each fly breathes independently. */
        val alpha: Float get() = (0.35f + 0.65f * (0.5f + 0.5f * sin(phase))).coerceIn(0f, 1f)
    }

    val flies = ArrayList<Firefly>()

    fun update(deltaSeconds: Float, active: Boolean, density: Float, bounds: Rect) {
        if (!active || density <= 0f) {
            if (flies.isNotEmpty()) flies.clear()
            return
        }
        val target = (density * MAX_FIREFLIES).toInt().coerceIn(0, MAX_FIREFLIES)
        while (flies.size < target) flies.add(spawn(bounds))
        while (flies.size > target) flies.removeAt(flies.size - 1)

        for (f in flies) {
            f.phase += f.blinkSpeed * deltaSeconds
            // Gentle Lissajous drift.
            f.x += sin(f.phase * 0.6f) * f.driftSpeed * deltaSeconds
            f.y += sin(f.phase * 0.9f + 1.3f) * f.driftSpeed * 0.6f * deltaSeconds
            wrap(f, bounds)
        }
    }

    private fun spawn(b: Rect) = Firefly(
        x = b.left + rng.nextFloat() * b.width,
        y = b.top + rng.nextFloat() * b.height,
        phase = rng.nextFloat() * 6.283f,
        driftSpeed = 6f + rng.nextFloat() * 10f,
        blinkSpeed = 0.8f + rng.nextFloat() * 1.4f,
    )

    private fun wrap(f: Firefly, b: Rect) {
        if (f.x < b.left) f.x = b.right
        if (f.x > b.right) f.x = b.left
        if (f.y < b.top) f.y = b.bottom
        if (f.y > b.bottom) f.y = b.top
    }

    private companion object { const val MAX_FIREFLIES = 40 }
}

// ---------------------------------------------------------------------------
//  RAIN — vertical drops for the Rest State; each spawns a ripple on landing.
// ---------------------------------------------------------------------------

class RainSystem(private val rng: Random = Random(0x2A1)) {
    class Drop(var x: Float, var y: Float, val speed: Float, val length: Float)

    val drops = ArrayList<Drop>()

    /** Steps drops; invokes [onSplash] with the x where a drop hits [groundY]. */
    fun update(
        deltaSeconds: Float,
        intensity: Float,
        bounds: Rect,
        groundY: Float,
        onSplash: (Float) -> Unit,
    ) {
        if (intensity <= 0f) {
            if (drops.isNotEmpty()) drops.clear()
            return
        }
        val target = (intensity * MAX_DROPS).toInt().coerceIn(0, MAX_DROPS)
        while (drops.size < target) drops.add(spawn(bounds))
        while (drops.size > target) drops.removeAt(drops.size - 1)

        for (d in drops) {
            d.y += d.speed * deltaSeconds
            if (d.y >= groundY) {
                onSplash(d.x)
                d.y = bounds.top - d.length
                d.x = bounds.left + rng.nextFloat() * bounds.width
            }
        }
    }

    private fun spawn(b: Rect) = Drop(
        x = b.left + rng.nextFloat() * b.width,
        y = b.top + rng.nextFloat() * b.height,
        speed = 700f + rng.nextFloat() * 400f,
        length = 14f + rng.nextFloat() * 14f,
    )

    private companion object { const val MAX_DROPS = 90 }
}

// ---------------------------------------------------------------------------
//  RIPPLES — expanding rings where rain meets water; also water-tile idle.
// ---------------------------------------------------------------------------

class RippleSystem {
    class Ripple(val x: Float, val y: Float, var age: Float, val maxAge: Float, val maxRadius: Float) {
        val progress: Float get() = (age / maxAge).coerceIn(0f, 1f)
        val radius: Float get() = maxRadius * progress
        val alpha: Float get() = (1f - progress).coerceIn(0f, 1f)
    }

    val ripples = ArrayList<Ripple>()

    fun spawn(x: Float, y: Float, maxRadius: Float = 22f) {
        if (ripples.size >= MAX_RIPPLES) return
        ripples.add(Ripple(x, y, age = 0f, maxAge = 0.9f, maxRadius = maxRadius))
    }

    fun update(deltaSeconds: Float) {
        val it = ripples.iterator()
        while (it.hasNext()) {
            val r = it.next()
            r.age += deltaSeconds
            if (r.age >= r.maxAge) it.remove()
        }
    }

    private companion object { const val MAX_RIPPLES = 60 }
}
