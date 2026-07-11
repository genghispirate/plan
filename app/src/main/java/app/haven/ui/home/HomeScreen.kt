package app.haven.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.haven.ui.diorama.DioramaFrame
import app.haven.ui.diorama.DioramaPlaceholder
import app.haven.ui.theme.Haven
import app.haven.ui.theme.HavenDimens
import app.haven.ui.zen.chromeAlpha
import app.haven.ui.zen.rememberZenModeState
import app.haven.ui.zen.zenInteraction

/**
 * The root home layout from Section 4: a framed diorama viewport occupying 45%
 * of the height above a 55% interface panel. The Haven Coin indicator floats in
 * the top corner of the frame; the Growth Points gauge lives in the bottom
 * utility sheet. Both, along with the panel, fade under Zen Mode — the diorama
 * itself never fades.
 *
 * Content here is structural: sample metrics and placeholder cards. Live
 * economy wiring and the real render canvas arrive in later steps.
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val zen = rememberZenModeState()
    val chromeAlpha by zen.chromeAlpha()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .zenInteraction(zen),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(HavenDimens.ScreenPadding),
        ) {
            // ---- Diorama viewport (45%) ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(HavenDimens.ViewportWeight),
            ) {
                DioramaFrame(modifier = Modifier.fillMaxSize()) {
                    DioramaPlaceholder()
                }
                HavenCoinIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(HavenDimens.FrameThickness + 6.dp)
                        .alpha(chromeAlpha),
                    coins = SAMPLE_HAVEN_COINS,
                )
            }

            Spacer(Modifier.height(HavenDimens.CardSpacing))

            // ---- Interface panel (55%) ----
            InterfacePanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(HavenDimens.PanelWeight)
                    .alpha(chromeAlpha),
            )
        }
    }
}

/** Small metallic Haven Coin chip — the top-corner consistency indicator. */
@Composable
private fun HavenCoinIndicator(coins: Long, modifier: Modifier = Modifier) {
    val colors = Haven.colors
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(colors.coinMetalHighlight, colors.coinMetal)),
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(colors.coinMetalHighlight),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$coins",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun InterfacePanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(HavenDimens.CardSpacing),
    ) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        FrostedCard {
            Column {
                Text(
                    "Focus session",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tap to begin a deep-work block.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        FrostedCard {
            Text(
                "Habit stack",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.weight(1f))

        // ---- Bottom utility sheet: Growth Points gauge ----
        GrowthPointsGauge(points = SAMPLE_GROWTH_POINTS)
    }
}

/** Frosted-glass utility workspace card (Section 4 body style). */
@Composable
private fun FrostedCard(content: @Composable () -> Unit) {
    val colors = Haven.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.frostedGlass)
            .border(1.dp, colors.frostedBorder, MaterialTheme.shapes.medium)
            .padding(HavenDimens.CardPadding),
    ) { content() }
}

/** Minimal clean numeric gauge for Growth Points, in the bottom utility sheet. */
@Composable
private fun GrowthPointsGauge(points: Long, modifier: Modifier = Modifier) {
    val colors = Haven.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.frostedGlass)
            .padding(horizontal = HavenDimens.CardPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(colors.growthGauge),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Growth",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            "$points GP",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private const val SAMPLE_HAVEN_COINS = 128L
private const val SAMPLE_GROWTH_POINTS = 2_450L
