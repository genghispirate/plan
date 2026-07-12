package app.haven.data.world

import kotlinx.serialization.Serializable

// ===========================================================================
//  HYDRATION  ->  water mechanics
//  Expands aquatic volume: muddy hole -> pond -> stream -> waterfall.
// ===========================================================================

@Serializable
enum class WaterFeatureType {
    MUD_HOLE,
    POND,
    STREAM,
    WATERFALL,
}

@Serializable
data class WaterFeature(
    val id: String,
    val at: GridCoordinate,
    val type: WaterFeatureType = WaterFeatureType.MUD_HOLE,
    /** Logical volume; drives physical size of the rendered aquatic element. */
    val volume: Float = 0f,
    /** Streams/waterfalls animate a pixel-shifting flow overlay when true. */
    val flowing: Boolean = false,
)

@Serializable
data class WaterSystem(
    val features: List<WaterFeature> = emptyList(),
    val totalVolume: Float = 0f,
)

// ===========================================================================
//  SLEEP CONSISTENCY  ->  lighting engines & fog clearance
//  High sleep quality clears the map mist and sets sunrise clarity.
// ===========================================================================

/**
 * The "Fog of War" over unexplored map edges. Sleep consistency reveals tiles
 * and raises overall clarity.
 */
@Serializable
data class FogOfWar(
    /** 0f = fully clear map, 1f = fully shrouded edges. */
    val density: Float = 1f,
    /** Grid tiles permanently revealed by accrued sleep consistency. */
    val revealedTiles: List<GridCoordinate> = emptyList(),
)

/**
 * Global lighting/atmosphere state the sky-box and shading read from.
 * [sunriseClarity] governs how vivid the morning state renders.
 */
@Serializable
data class Atmosphere(
    val fog: FogOfWar = FogOfWar(),
    /** 0f = hazy, 1f = crystalline sunrise. Set by recent sleep quality. */
    val sunriseClarity: Float = 0.5f,
    /** Ambient light multiplier applied on top of the current [DayPhase]. */
    val ambientLightScale: Float = 1f,
)
