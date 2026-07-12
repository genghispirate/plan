package app.haven.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.haven.data.local.entity.PendingClaimEntity
import app.haven.ui.claim.ClaimCard
import app.haven.ui.diorama.DioramaFrame
import app.haven.ui.diorama.DioramaPlaceholder
import app.haven.ui.render.DioramaCanvas
import app.haven.ui.render.rememberAmbientStandby
import app.haven.ui.theme.Haven
import app.haven.ui.theme.HavenDimens
import app.haven.ui.zen.chromeAlpha
import app.haven.ui.zen.rememberZenModeState
import app.haven.ui.zen.zenInteraction

/**
 * The root home layout (Section 4): a framed diorama viewport over an interface
 * panel, 45 / 55. The live world drives the render Canvas; the two economy
 * metrics sit in the coin chip and growth gauge. Chrome fades under Zen Mode,
 * and Ambient Standby (landscape + charging) hides the panel entirely and lets
 * the diorama fill the screen edge-to-edge with a sky clock.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val world by viewModel.world.collectAsStateWithLifecycle()
    val growthPoints by viewModel.growthPoints.collectAsStateWithLifecycle()
    val havenCoins by viewModel.havenCoins.collectAsStateWithLifecycle()
    val pendingClaims by viewModel.pendingClaims.collectAsStateWithLifecycle()

    val standby = rememberAmbientStandby()
    val zen = rememberZenModeState()
    val zenAlpha by zen.chromeAlpha()
    // Standby fully hides chrome; otherwise Zen Mode drives the fade.
    val chromeAlpha = if (standby) 0f else zenAlpha

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .zenInteraction(zen),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (standby) Modifier else Modifier.windowInsetsPadding(WindowInsets.safeDrawing))
                .padding(if (standby) 0.dp else HavenDimens.ScreenPadding),
        ) {
            // ---- Diorama viewport (45%, or full-bleed in standby) ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (standby) 1f else HavenDimens.ViewportWeight),
            ) {
                val dioramaContent: @Composable () -> Unit = {
                    val w = world
                    if (w != null) {
                        DioramaCanvas(world = w, standby = standby, modifier = Modifier.fillMaxSize())
                    } else {
                        DioramaPlaceholder()
                    }
                }

                if (standby) {
                    // Edge-to-edge, no frame.
                    dioramaContent()
                } else {
                    DioramaFrame(modifier = Modifier.fillMaxSize()) { dioramaContent() }
                    HavenCoinIndicator(
                        coins = havenCoins,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(HavenDimens.FrameThickness + 6.dp)
                            .alpha(chromeAlpha),
                    )
                }
            }

            if (!standby) {
                Spacer(Modifier.height(HavenDimens.CardSpacing))
                InterfacePanel(
                    growthPoints = growthPoints,
                    pendingClaims = pendingClaims,
                    onClaim = viewModel::onClaim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(HavenDimens.PanelWeight)
                        .alpha(chromeAlpha),
                )
            }
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
private fun InterfacePanel(
    growthPoints: Long,
    pendingClaims: List<PendingClaimEntity>,
    onClaim: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(HavenDimens.CardSpacing),
    ) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Banked rewards awaiting the deliberate Claim ritual.
        pendingClaims.forEach { claim ->
            ClaimCard(claim = claim, onClaim = onClaim)
        }

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

        GrowthPointsGauge(points = growthPoints)
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
