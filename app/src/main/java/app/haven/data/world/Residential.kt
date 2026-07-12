package app.haven.data.world

import kotlinx.serialization.Serializable

// ===========================================================================
//  CLEANING & HOUSEWORK  ->  residential quality
//  Upgrades dwellings: canvas tent -> timber cabin -> ... -> mid-century modern.
// ===========================================================================

/** Dwelling appearance progression, driven by cleaning/housework consistency. */
@Serializable
enum class DwellingTier {
    CANVAS_TENT,
    TIMBER_CABIN,
    COTTAGE,
    TOWNHOUSE,
    MID_CENTURY_MODERN,
}

@Serializable
data class Dwelling(
    val id: String,
    val at: GridCoordinate,
    val tier: DwellingTier = DwellingTier.CANVAS_TENT,
    val occupantIds: List<String> = emptyList(),
    /** Smoke rises from the chimney only when the world is not resting. */
    val chimneySmoke: Boolean = true,
)

/**
 * A resident. During a Rest State villagers move indoors ([indoors] = true)
 * rather than being harmed — the world stagnates but stays safe.
 */
@Serializable
data class Villager(
    val id: String,
    val homeDwellingId: String? = null,
    val indoors: Boolean = false,
)

@Serializable
data class Residential(
    val dwellings: List<Dwelling> = emptyList(),
    val villagers: List<Villager> = emptyList(),
)
