package app.haven.data.model

import kotlinx.serialization.Serializable

/**
 * The six real-world habit categories from the Habit-to-World graphics matrix,
 * plus FOCUS for in-app deep-work sessions. Each category deterministically
 * drives one graphical array of the diorama (see [app.haven.data.world]).
 */
@Serializable
enum class HabitCategory {
    EXERCISE,   // infrastructure pathways
    READING,    // intellectual district / library
    MEDITATION, // natural biodiversity
    CLEANING,   // residential quality
    HYDRATION,  // water mechanics
    SLEEP,      // lighting engines & fog clearance
    FOCUS,      // in-app focus timer (fuels GP directly)
}

/** The compact two-metric economy (Section 2). Nothing else is a currency. */
@Serializable
enum class RewardMetric {
    /** Volume-based output. Auto-fuels infrastructure evolution & mist clearing. */
    GROWTH_POINTS,

    /** Time-based consistency. Spent voluntarily in the offline market. */
    HAVEN_COINS,
}

/** Provenance of a habit signal, so we can distinguish auto-detected from manual. */
enum class DataSource {
    HEALTH_CONNECT,
    MANUAL,
    FOCUS_TIMER,
    INTERCEPTOR_GATE,
}

/**
 * Lifecycle of a banked reward for the "Claim" ritual (dopamine delayer).
 * Rewards are detected in the background but only applied on deliberate claim.
 */
enum class ClaimStatus {
    PENDING,
    CLAIMED,
    EXPIRED,
}

/** The micro-habit a user must actively complete to override an app block. */
enum class GateType {
    HYDRATION_LOG,
    PHILOSOPHY_QUOTE,
    BREATH_CYCLE,
}

/** Where an encrypted backup archive is written. */
enum class BackupTargetType {
    LOCAL_SAF,   // user-chosen document via Storage Access Framework
    WEBDAV,      // personal self-hosted WebDAV directory
}

/** Progress of a single backup run through the local serialization pipeline. */
enum class BackupStatus {
    QUEUED,
    SERIALIZING,
    ENCRYPTING,
    TRANSFERRING,
    COMPLETE,
    FAILED,
}
