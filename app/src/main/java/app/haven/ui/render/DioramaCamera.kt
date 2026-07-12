package app.haven.ui.render

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Spring-driven camera for the diorama (Section 4.B). Focusing a structure
 * glides the pan/zoom to its coordinates and interpolates the building's roof
 * alpha to 0 so the interior floor plan is revealed; [reset] restores the roof.
 *
 * Every transition uses the Section 4.A sheet spring and is fully interruptible
 * — re-focusing mid-animation simply retargets the same Animatables, inheriting
 * their current velocity without snapping.
 */
class DioramaCamera {
    val offset = Animatable(Offset.Zero, Offset.VectorConverter)
    val zoom = Animatable(1f)
    /** 1f = roof opaque (normal), 0f = roof faded (focused interior). */
    val roofAlpha = Animatable(1f)

    /** Which structure id is currently focused, or null when zoomed out. */
    var focusedId: String? = null
        private set

    private fun <T> sheetSpec() = spring<T>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy,
    )

    suspend fun focusOn(id: String, worldPan: Offset, targetZoom: Float = 2.2f) {
        focusedId = id
        coroutineScope {
            launch { offset.animateTo(worldPan, sheetSpec()) }
            launch { zoom.animateTo(targetZoom, sheetSpec()) }
            launch { roofAlpha.animateTo(0f, sheetSpec()) }
        }
    }

    suspend fun reset() {
        focusedId = null
        coroutineScope {
            launch { offset.animateTo(Offset.Zero, sheetSpec()) }
            launch { zoom.animateTo(1f, sheetSpec()) }
            launch { roofAlpha.animateTo(1f, sheetSpec()) }
        }
    }
}
