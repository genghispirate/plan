package app.haven.ui.claim

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.haven.data.local.entity.PendingClaimEntity
import app.haven.ui.theme.Haven
import app.haven.ui.theme.HavenMotion

/**
 * A heavy, tactile card for a single banked reward (Section 1.2). The deliberate
 * "Claim" press — compressing on the Section 4.A pop spring — is what isolates
 * the real-world achievement and maps it to the visual dopamine hit of the world
 * upgrading when [onClaim] runs the evolution.
 */
@Composable
fun ClaimCard(
    claim: PendingClaimEntity,
    onClaim: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Haven.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.frostedGlass, MaterialTheme.shapes.large)
            .border(1.dp, colors.frameKeyline.copy(alpha = 0.4f), MaterialTheme.shapes.large)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Ready to claim",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = claim.summary,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (claim.growthPoints > 0) {
                RewardChip("+${claim.growthPoints} GP", colors.growthGauge)
                Spacer(Modifier.width(8.dp))
            }
            if (claim.havenCoins > 0) {
                RewardChip("+${claim.havenCoins} HC", colors.coinMetal)
            }
        }
        ClaimButton(onClick = { onClaim(claim.id) })
    }
}

@Composable
private fun RewardChip(label: String, tint: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(tint.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = tint, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ClaimButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = HavenMotion.pop(),
        label = "claimPop",
    )
    Button(
        onClick = onClick,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
    ) {
        Text("Claim", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}
