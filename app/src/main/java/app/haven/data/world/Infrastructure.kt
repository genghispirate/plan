package app.haven.data.world

import kotlinx.serialization.Serializable

// ===========================================================================
//  EXERCISE & FITNESS  ->  infrastructure pathways
//  GP earned here upgrades dirt tracks, builds bridges, lays flagstone squares.
// ===========================================================================

/** Path surface progression, driven by cumulative Exercise GP. */
@Serializable
enum class PathSurface {
    DIRT,
    GRAVEL,
    PAVED,
    FLAGSTONE,
}

/** A traversable segment between two grid nodes. */
@Serializable
data class PathSegment(
    val id: String,
    val from: GridCoordinate,
    val to: GridCoordinate,
    val surface: PathSurface = PathSurface.DIRT,
)

/** A bridge spanning a river/water feature; unlocks new buildable land. */
@Serializable
data class Bridge(
    val id: String,
    val at: GridCoordinate,
    val spanTiles: Int,
    val tier: EvolutionTier = EvolutionTier.EMERGING,
)

/** A gathering plaza. Paving state and size scale with sustained activity. */
@Serializable
data class TownSquare(
    val id: String,
    val at: GridCoordinate,
    val radiusTiles: Float,
    val surface: PathSurface = PathSurface.DIRT,
    val tier: EvolutionTier = EvolutionTier.SEED,
)

@Serializable
data class Infrastructure(
    val paths: List<PathSegment> = emptyList(),
    val bridges: List<Bridge> = emptyList(),
    val squares: List<TownSquare> = emptyList(),
)
