package app.haven.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp

/**
 * Layout tokens from Section 4. The 45 / 55 split between the diorama viewport
 * and the interface panel is expressed as weights consumed by the home layout.
 */
object HavenDimens {
    const val ViewportWeight = 0.45f
    const val PanelWeight = 0.55f

    val ScreenPadding = 16.dp
    val CardPadding = 20.dp
    val CardSpacing = 12.dp

    // Diorama frame (walnut border + brass keyline).
    val FrameThickness = 14.dp
    val FrameKeyline = 1.5.dp
    val FrameCorner = 24.dp
}

/**
 * The two motion specs from Section 4.A, centralized so every sheet/card and
 * micro-interaction across the app pulls from one source of truth.
 */
object HavenMotion {
    /** UI sheets & cards: settle without bounce. */
    fun <T> sheet() = spring<T>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy,
    )

    /** Checkboxes / button pops: snappy with a touch of overshoot. */
    fun <T> pop() = spring<T>(
        stiffness = Spring.StiffnessHigh,
        dampingRatio = Spring.DampingRatioLowBouncy,
    )
}
