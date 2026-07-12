package app.haven.data.world

import kotlinx.serialization.Serializable

// ===========================================================================
//  MEDITATION & MENTAL HEALTH  ->  natural biodiversity
//  Generates foliage, botanical gardens, ancient trees, and the nocturnal
//  firefly particle system.
// ===========================================================================

@Serializable
enum class FoliageType {
    GRASS_TUFT,
    SHRUB,
    FLOWER_BED,
    FERN,
}

@Serializable
data class Foliage(
    val id: String,
    val at: GridCoordinate,
    val type: FoliageType = FoliageType.GRASS_TUFT,
)

@Serializable
data class Tree(
    val id: String,
    val at: GridCoordinate,
    val tier: EvolutionTier = EvolutionTier.SEED,
    /** Ancient trees are canopy-defining landmarks unlocked at high meditation. */
    val ancient: Boolean = false,
)

@Serializable
data class BotanicalGarden(
    val id: String,
    val at: GridCoordinate,
    val radiusTiles: Float,
    val tier: EvolutionTier = EvolutionTier.EMERGING,
)

/**
 * Nocturnal firefly emitter config. Activated during MIDNIGHT phase; the
 * renderer drives the float-and-fade yellow vectors (Section 4.C).
 */
@Serializable
data class FireflySystem(
    val active: Boolean = false,
    /** 0f..1f particle density, scales with meditation consistency. */
    val density: Float = 0f,
)

@Serializable
data class Biodiversity(
    val foliage: List<Foliage> = emptyList(),
    val trees: List<Tree> = emptyList(),
    val gardens: List<BotanicalGarden> = emptyList(),
    val fireflies: FireflySystem = FireflySystem(),
)
