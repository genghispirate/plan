package app.haven.data.world

import kotlinx.serialization.Serializable

/**
 * The complete, self-contained serializable model of a user's diorama.
 *
 * This is the single JSON object referenced throughout the spec: it is what
 * gets serialized into the daily encrypted backup, what the renderer (Step 5)
 * reads to draw every frame, and what each claimed reward mutates. It holds no
 * Android types so it stays pure, testable, and forward/backward compatible.
 *
 * Every field carries a default so older archives deserialize cleanly as the
 * schema grows; bump [schemaVersion] and add migrations in the serializer when
 * a breaking change is unavoidable.
 */
@Serializable
data class WorldState(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val worldId: String,
    /** Deterministic seed for procedural placement, kept stable for a world. */
    val seed: Long,
    val createdAtEpochMillis: Long,
    val lastEvolvedAtEpochMillis: Long,

    // --- Atmosphere & time ---
    val weather: WeatherPhase = WeatherPhase.SUNSHINE,
    val restState: RestState = RestState(),
    val circadian: CircadianProfile = CircadianProfile(),
    val dayPhase: DayPhase = DayPhase.DAYLIGHT,
    val atmosphere: Atmosphere = Atmosphere(),

    // --- Economy cache ---
    val economy: EconomySnapshot = EconomySnapshot(),

    // --- The six habit-driven graphical arrays ---
    val infrastructure: Infrastructure = Infrastructure(),   // Exercise
    val intellectualDistrict: IntellectualDistrict = IntellectualDistrict(), // Reading
    val biodiversity: Biodiversity = Biodiversity(),         // Meditation
    val residential: Residential = Residential(),            // Cleaning
    val water: WaterSystem = WaterSystem(),                  // Hydration

    // --- Permanent ownership choices ---
    val aesthetics: AestheticChoices = AestheticChoices(),
) {
    companion object {
        /** Increment on any breaking change to the serialized shape. */
        const val CURRENT_SCHEMA_VERSION: Int = 1

        /**
         * A pristine starting world: a single canvas tent, one villager, a muddy
         * hole, fully shrouded fog. Everything else emerges through real habits.
         */
        fun newWorld(
            worldId: String,
            seed: Long,
            nowEpochMillis: Long,
        ): WorldState = WorldState(
            worldId = worldId,
            seed = seed,
            createdAtEpochMillis = nowEpochMillis,
            lastEvolvedAtEpochMillis = nowEpochMillis,
            residential = Residential(
                dwellings = listOf(
                    Dwelling(
                        id = "dwelling-hearth",
                        at = GridCoordinate(0f, 0f),
                        tier = DwellingTier.CANVAS_TENT,
                        occupantIds = listOf("villager-founder"),
                    ),
                ),
                villagers = listOf(
                    Villager(id = "villager-founder", homeDwellingId = "dwelling-hearth"),
                ),
            ),
            water = WaterSystem(
                features = listOf(
                    WaterFeature(
                        id = "water-source",
                        at = GridCoordinate(2f, 1f),
                        type = WaterFeatureType.MUD_HOLE,
                    ),
                ),
            ),
        )
    }
}
