package app.haven.ui.diorama

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import app.haven.ui.theme.Haven

/**
 * A static stand-in for the diorama while the render pipeline is built in
 * Step 5: a soft sky gradient with a single rolling hill, using the theme's
 * environmental colors. The real Canvas layer controller and particle systems
 * replace this composable wholesale.
 */
@Composable
fun DioramaPlaceholder(modifier: Modifier = Modifier) {
    val colors = Haven.colors
    Canvas(modifier = modifier.fillMaxSize()) {
        // Sky
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(colors.skyTop, colors.skyBottom),
            ),
        )
        // Rolling ground silhouette
        val hill = Path().apply {
            moveTo(0f, size.height * 0.72f)
            quadraticBezierTo(
                size.width * 0.5f, size.height * 0.52f,
                size.width, size.height * 0.70f,
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = hill, color = colors.growthGauge)

        // A hint of water pooled at the base.
        drawCircle(
            color = colors.water,
            radius = size.minDimension * 0.10f,
            center = Offset(size.width * 0.32f, size.height * 0.86f),
        )
    }
}
