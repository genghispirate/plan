package app.haven.data.world

import kotlinx.serialization.Serializable

// ===========================================================================
//  READING & DEEP STUDY  ->  intellectual district
//  Logged hours progressively construct the central Library: wings, reading
//  rooms, and outdoor study desks.
// ===========================================================================

/** A named wing that alpha-fades its roof when the user zooms in (Section 4.B). */
@Serializable
data class LibraryWing(
    val id: String,
    val name: String,
    val at: GridCoordinate,
    val tier: EvolutionTier = EvolutionTier.SEED,
    /** 1f = roof fully opaque, 0f = interior revealed. Animated by the renderer. */
    val roofAlpha: Float = 1f,
)

@Serializable
data class ReadingRoom(
    val id: String,
    val at: GridCoordinate,
    val capacity: Int = 2,
)

@Serializable
data class StudyDesk(
    val id: String,
    val at: GridCoordinate,
    val outdoor: Boolean = true,
)

@Serializable
data class IntellectualDistrict(
    val libraryTier: EvolutionTier = EvolutionTier.SEED,
    val wings: List<LibraryWing> = emptyList(),
    val readingRooms: List<ReadingRoom> = emptyList(),
    val studyDesks: List<StudyDesk> = emptyList(),
    /** Cumulative reading minutes logged; the construction fuel gauge. */
    val totalReadingMinutes: Long = 0L,
)
