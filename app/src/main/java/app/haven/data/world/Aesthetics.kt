package app.haven.data.world

import kotlinx.serialization.Serializable

// ===========================================================================
//  ENDOWMENT EFFECT VIA CHOICE (Section 1.3)
//  Three permanent aesthetic choices made in the first 3 onboarding days to
//  establish instant psychological ownership. Once locked, they never change.
// ===========================================================================

@Serializable
enum class HearthWoodGrain {
    WALNUT,
    OAK,
    ASH,
    EBONY,
}

@Serializable
enum class PathLayoutStyle {
    WINDING,
    ORGANIC,
    GEOMETRIC,
}

@Serializable
enum class HearthStonework {
    RIVER_STONE,
    SLATE,
    TERRACOTTA,
}

/**
 * The three irreversible onboarding choices. A null value means "not yet
 * chosen"; [locked] flips true the moment the choice is committed and the
 * corresponding day's ritual completes.
 */
@Serializable
data class AestheticChoices(
    val hearthWoodGrain: HearthWoodGrain? = null,
    val pathLayout: PathLayoutStyle? = null,
    val hearthStonework: HearthStonework? = null,
    val locked: Boolean = false,
)

// ===========================================================================
//  ECONOMY SNAPSHOT (Section 2)
//  A denormalized cache of the two metrics for fast render. The authoritative
//  ledger lives in Room; this is refreshed whenever the world is persisted.
// ===========================================================================

@Serializable
data class EconomySnapshot(
    /** Growth Points — volume-based, auto-fuels evolution. */
    val growthPoints: Long = 0L,
    /** Haven Coins — consistency-based, spent in the offline market. */
    val havenCoins: Long = 0L,
)
