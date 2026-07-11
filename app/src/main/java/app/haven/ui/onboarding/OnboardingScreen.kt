package app.haven.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.haven.domain.onboarding.AestheticOption
import app.haven.domain.onboarding.OnboardingStep
import app.haven.domain.onboarding.OptionMotif
import app.haven.ui.theme.Haven
import app.haven.ui.theme.HavenMotion
import kotlin.math.sin

/**
 * Full-screen founding ritual for one onboarding step. The user must commit a
 * choice to proceed — the copy leans into permanence, and the tactile selection
 * (Section 4.A springs) makes the pick feel weighty, driving the Endowment
 * Effect. There is deliberately no skip.
 */
@Composable
fun OnboardingScreen(
    step: OnboardingStep,
    completedCount: Int,
    onCommit: (OnboardingStep, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedId by remember(step.key) { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepDots(current = step.dayIndex, completed = completedCount)
        Spacer(Modifier.height(28.dp))

        Text(
            text = step.title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = step.prompt,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            step.options.forEach { option ->
                OptionTile(
                    option = option,
                    selected = option.id == selectedId,
                    onSelect = { selectedId = option.id },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        val selected = selectedId
        Button(
            onClick = { if (selected != null) onCommit(step, selected) },
            enabled = selected != null,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text("Set permanently", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "This can't be undone.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic,
        )
    }
}

@Composable
private fun StepDots(current: Int, completed: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 1..OnboardingStep.TOTAL) {
            val filled = i <= completed || i == current
            Box(
                Modifier
                    .size(if (i == current) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    ),
            )
        }
    }
}

@Composable
private fun OptionTile(
    option: AestheticOption,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = HavenMotion.pop(),
        label = "optionPop",
    )
    val borderColor = if (selected) Haven.colors.frameKeyline else Color.Transparent

    Column(
        modifier = modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.medium)
            .border(2.dp, borderColor, MaterialTheme.shapes.medium)
            .clickable(onClick = onSelect)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp)),
        ) {
            OptionPreview(option, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

/** Draws a swatch, or a representative path motif for the path-layout step. */
@Composable
private fun OptionPreview(option: AestheticOption, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(option.previewColor.copy(alpha = 0.22f))) {
        when (option.motif) {
            OptionMotif.SWATCH -> drawRect(option.previewColor)
            else -> {
                val path = motifPath(option.motif)
                drawPath(
                    path = path,
                    color = option.previewColor,
                    style = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.motifPath(motif: OptionMotif): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        when (motif) {
            OptionMotif.PATH_WINDING -> {
                moveTo(w * 0.12f, h * 0.5f)
                var x = 0.12f
                while (x <= 0.88f) {
                    lineTo(w * x, h * (0.5f + 0.28f * sin((x - 0.12f) * 12f)))
                    x += 0.04f
                }
            }
            OptionMotif.PATH_ORGANIC -> {
                moveTo(w * 0.15f, h * 0.75f)
                cubicTo(w * 0.30f, h * 0.20f, w * 0.65f, h * 0.95f, w * 0.85f, h * 0.30f)
            }
            OptionMotif.PATH_GEOMETRIC -> {
                moveTo(w * 0.15f, h * 0.80f)
                lineTo(w * 0.15f, h * 0.35f)
                lineTo(w * 0.55f, h * 0.35f)
                lineTo(w * 0.55f, h * 0.65f)
                lineTo(w * 0.85f, h * 0.65f)
            }
            OptionMotif.SWATCH -> {}
        }
    }
}
