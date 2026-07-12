package app.haven.data.world

import kotlinx.serialization.Serializable

/**
 * A position on the 2.5D isometric grid. Units are logical tiles, not pixels —
 * the renderer (Step 5) projects these to screen space, so world state stays
 * resolution-independent and serialization-stable.
 */
@Serializable
data class GridCoordinate(
    val x: Float,
    val y: Float,
    /** Optional stacking height for elevated tiles (bridges, elevated squares). */
    val z: Float = 0f,
)

/** Coarse quality/evolution tier shared by many upgradable structures. */
@Serializable
enum class EvolutionTier {
    SEED,       // just placed / rudimentary
    EMERGING,
    ESTABLISHED,
    REFINED,
    PREMIUM,    // artisan / mid-century-modern grade
}

// ---------------------------------------------------------------------------
//  WEATHER & THE "ABSENCE OF GUILT" REST STATE (Section 1.1)
// ---------------------------------------------------------------------------

/** The atmospheric mood of the whole diorama. Never a "failure" state. */
@Serializable
enum class WeatherPhase {
    SUNSHINE,   // healthy, active world
    OVERCAST,   // entering rest — soft, safe stagnation
    RAINY,      // full rest state — villagers indoors, smoke stopped
}

/** Why the world entered a Rest State. Purely diagnostic; never shown as blame. */
@Serializable
enum class RestTrigger {
    NONE,
    STREAK_BROKEN,
    FOCUS_SESSION_FAILED,
    APP_OVERUSE,
}

/**
 * The guilt-free stagnation state. When active, construction pauses and smoke
 * stops, but nothing is destroyed. Completing any real habit clears it.
 */
@Serializable
data class RestState(
    val isResting: Boolean = false,
    val trigger: RestTrigger = RestTrigger.NONE,
    val enteredAtEpochMillis: Long = 0L,
    /** 0f = fully sunny, 1f = fully overcast/rainy. Drives smooth lighting lerp. */
    val intensity: Float = 0f,
)

// ---------------------------------------------------------------------------
//  DYNAMIC CIRCADIAN ENGINE (Section 5.1)
// ---------------------------------------------------------------------------

/** Discrete lighting phases the diorama's sky box cross-fades between. */
@Serializable
enum class DayPhase {
    SUNRISE,
    GOLDEN_HOUR,
    DAYLIGHT,
    SUNSET,
    DUSK,
    MIDNIGHT,
}

/** Predefined waking-cycle archetypes; CUSTOM uses the explicit minute offsets. */
@Serializable
enum class ShiftArchetype {
    STANDARD,
    EARLY_BIRD,
    NIGHT_SHIFT,
    ROTATING,
    CUSTOM,
}

/**
 * Maps the diorama's golden hour / midnight / sunrise to the user's *actual*
 * waking day rather than local wall-clock time. A hospitality night-shift user
 * sees golden hour when they wake in the afternoon.
 */
@Serializable
data class CircadianProfile(
    val archetype: ShiftArchetype = ShiftArchetype.STANDARD,
    /** Minutes past local midnight the user typically wakes (e.g. 420 = 07:00). */
    val wakeMinuteOfDay: Int = 420,
    /** Minutes past local midnight the user typically sleeps (e.g. 1380 = 23:00). */
    val sleepMinuteOfDay: Int = 1380,
)
