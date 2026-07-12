package app.haven.intercept.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.haven.data.model.GateType
import app.haven.intercept.gate.ActionGateRequest
import app.haven.intercept.gate.ActionGateResult
import kotlinx.coroutines.delay

/**
 * The Action Gate interstitial. Presented over a blocked app, it forces an
 * active mental transition: the primary "override" control stays disabled until
 * the gate's micro-habit is genuinely completed, never merely waited out.
 *
 * Visuals are intentionally restrained here — the premium serif/frosted theme
 * and palette land in Step 4; this focuses on the interaction contract and the
 * physics-spring motion spec (Section 4.A).
 */
@Composable
fun ActionGateScreen(
    request: ActionGateRequest,
    onResult: (ActionGateResult) -> Unit,
) {
    // Whole-panel entrance obeys the sheet spring (medium-low / no bounce).
    val entrance = remember { Animatable(0.92f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(
            1f,
            spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        )
    }

    var completed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Overcast scrim echoes the "Rest State" mood rather than an alarm.
            .background(Color(0xE60E1116)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF141A22),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .scale(entrance.value)
                .padding(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "A moment first",
                    color = Color(0xFFEDE6D8),
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "${request.appLabel} can wait a breath. Complete one small thing to continue.",
                    color = Color(0xFF9AA4B2),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )

                // Gate-specific micro-habit. Reports completion upward.
                when (request.gateType) {
                    GateType.PHILOSOPHY_QUOTE -> QuoteGate(request) { completed = true }
                    GateType.HYDRATION_LOG -> HydrationGate { completed = true }
                    GateType.BREATH_CYCLE -> BreathGate { completed = true }
                }

                SpringButton(
                    label = "Continue to ${request.appLabel}",
                    enabled = completed,
                    onClick = { onResult(ActionGateResult.Overridden(request.gateType)) },
                )
                TextButton(onClick = { onResult(ActionGateResult.Left) }) {
                    Text("Not now — take me home", color = Color(0xFF7E8896))
                }
            }
        }
    }
}

/** A curated quote that must be dwelt on for [DWELL_SECONDS] before proceeding. */
@Composable
private fun QuoteGate(request: ActionGateRequest, onComplete: () -> Unit) {
    val quote = request.quote ?: return
    var remaining by remember { mutableStateOf(DWELL_SECONDS) }
    LaunchedEffect(request.seed) {
        while (remaining > 0) {
            delay(1_000)
            remaining -= 1
        }
        onComplete()
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "“${quote.text}”",
            color = Color(0xFFEDE6D8),
            fontSize = 18.sp,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
        )
        Text(
            text = "— ${quote.author}",
            color = Color(0xFF9AA4B2),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
        if (remaining > 0) {
            Text(
                text = "Take it in… $remaining",
                color = Color(0xFF5C6570),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}

/** Tap the glass to log a single glass of water; the tactile pour enables pass. */
@Composable
private fun HydrationGate(onComplete: () -> Unit) {
    var poured by remember { mutableStateOf(false) }
    val fill = remember { Animatable(0f) }
    LaunchedEffect(poured) {
        if (poured) {
            fill.animateTo(
                1f,
                spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
            )
            onComplete()
        }
    }
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E2732))
            .clickable(enabled = !poured) { poured = true },
        contentAlignment = Alignment.Center,
    ) {
        // Water rising inside the glass, height tracks the spring-animated fill.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = (120 * (1f - fill.value)).dp)
                .background(Color(0xFF3E6E8E)),
        )
        Text(
            text = if (poured) "Logged" else "Tap to\nlog water",
            color = Color(0xFFEDE6D8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** One guided inhale-hold-exhale cycle. Content pacing uses timed easing. */
@Composable
private fun BreathGate(onComplete: () -> Unit) {
    val scale = remember { Animatable(0.55f) }
    var phase by remember { mutableStateOf("Breathe in") }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(4_000, easing = FastOutSlowInEasing))
        phase = "Hold"
        delay(2_000)
        phase = "Breathe out"
        scale.animateTo(0.55f, tween(4_000, easing = FastOutSlowInEasing))
        phase = "Done"
        onComplete()
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale.value)
                .clip(CircleShape)
                .background(Color(0xFF2C4A57)),
        )
        Text(
            text = phase,
            color = Color(0xFFEDE6D8),
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

/**
 * Primary control with the micro-interaction spring (high stiffness, low
 * bounce): a tactile compress on press that pops back on release.
 */
@Composable
private fun SpringButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioLowBouncy,
        ),
        label = "buttonPop",
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFCBA36A),
            contentColor = Color(0xFF141A22),
            disabledContainerColor = Color(0xFF2A3340),
            disabledContentColor = Color(0xFF5C6570),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

private const val DWELL_SECONDS = 6
