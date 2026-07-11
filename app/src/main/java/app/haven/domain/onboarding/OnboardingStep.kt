package app.haven.domain.onboarding

import androidx.compose.ui.graphics.Color

/** How an option previews itself in the picker. */
enum class OptionMotif { SWATCH, PATH_WINDING, PATH_ORGANIC, PATH_GEOMETRIC }

/**
 * A single selectable aesthetic option. [id] is the world-model enum name so it
 * round-trips straight into [app.haven.data.world.AestheticChoices].
 */
data class AestheticOption(
    val id: String,
    val label: String,
    val previewColor: Color,
    val motif: OptionMotif = OptionMotif.SWATCH,
)

private val WoodGrains = listOf(
    AestheticOption("WALNUT", "Walnut", Color(0xFF4A3524)),
    AestheticOption("OAK", "Oak", Color(0xFFB08D57)),
    AestheticOption("ASH", "Ash", Color(0xFFD9CBB2)),
    AestheticOption("EBONY", "Ebony", Color(0xFF2A211B)),
)

private val PathLayouts = listOf(
    AestheticOption("WINDING", "Winding", Color(0xFF8A6D4B), OptionMotif.PATH_WINDING),
    AestheticOption("ORGANIC", "Organic", Color(0xFF6E8B5A), OptionMotif.PATH_ORGANIC),
    AestheticOption("GEOMETRIC", "Geometric", Color(0xFF9BA0A6), OptionMotif.PATH_GEOMETRIC),
)

private val Stoneworks = listOf(
    AestheticOption("RIVER_STONE", "River stone", Color(0xFF8A94A6)),
    AestheticOption("SLATE", "Slate", Color(0xFF3A4552)),
    AestheticOption("TERRACOTTA", "Terracotta", Color(0xFFC06E52)),
)

/**
 * The three irreversible onboarding rituals (Section 1.3), one per day across
 * the first three days. Each commits a permanent aesthetic to establish instant
 * psychological ownership. [dayIndex] is 1-based; the choice unlocks that many
 * days into the app's life.
 */
enum class OnboardingStep(
    val dayIndex: Int,
    val key: String,
    val title: String,
    val prompt: String,
    val options: List<AestheticOption>,
) {
    HEARTH_WOOD_GRAIN(
        dayIndex = 1,
        key = "hearth_wood_grain",
        title = "Choose your hearth",
        prompt = "Pick the wood grain of the central hearth. This choice is permanent — it becomes uniquely yours.",
        options = WoodGrains,
    ),
    PATH_LAYOUT(
        dayIndex = 2,
        key = "path_layout",
        title = "Lay the first path",
        prompt = "Decide how your primary dirt path is drawn. Once set, it can never be changed.",
        options = PathLayouts,
    ),
    HEARTH_STONEWORK(
        dayIndex = 3,
        key = "hearth_stonework",
        title = "Set the hearthstone",
        prompt = "Choose the stonework framing your hearth. This is the last of your founding choices, and it is forever.",
        options = Stoneworks,
    );

    companion object {
        val ordered: List<OnboardingStep> = entries.sortedBy { it.dayIndex }
        const val TOTAL = 3
    }
}
