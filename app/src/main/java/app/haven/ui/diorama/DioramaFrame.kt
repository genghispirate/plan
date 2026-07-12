package app.haven.ui.diorama

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import app.haven.ui.theme.Haven
import app.haven.ui.theme.HavenDimens

/**
 * The fixed "Walnut / Brass Border" framing from Section 4. Renders a milled
 * wooden bezel with an inset brass keyline around whatever diorama [content] is
 * placed inside — the render canvas in Step 5 slots straight in here.
 *
 * The frame is a stack: outer walnut gradient -> inner shadow lip -> brass
 * hairline -> clipped content well.
 */
@Composable
fun DioramaFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = Haven.colors
    val frame = HavenDimens.FrameThickness
    val corner = HavenDimens.FrameCorner
    val keylinePx = HavenDimens.FrameKeyline

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    listOf(colors.frameOuter, colors.frameInner),
                ),
            )
            .padding(frame)
            // Brass keyline traced just inside the wooden bezel.
            .drawWithContent {
                drawContent()
                val inset = keylinePx.toPx() / 2f
                drawRoundRect(
                    color = colors.frameKeyline,
                    topLeft = Offset(-inset, -inset),
                    size = Size(size.width + inset * 2, size.height + inset * 2),
                    cornerRadius = CornerRadius((corner - frame).toPx().coerceAtLeast(0f)),
                    style = Stroke(width = keylinePx.toPx()),
                )
            }
            .clip(RoundedCornerShape((corner - frame / 2)))
            .background(colors.skyBottom),
    ) {
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}
