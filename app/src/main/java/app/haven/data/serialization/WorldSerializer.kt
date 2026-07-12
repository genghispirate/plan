package app.haven.data.serialization

import app.haven.data.world.WorldState
import kotlinx.serialization.json.Json

/**
 * Central kotlinx.serialization configuration for the whole app.
 *
 *  - `ignoreUnknownKeys` + `encodeDefaults` let the schema grow without
 *    breaking older archives.
 *  - `prettyPrint` is off so the serialized world stays compact inside the
 *    encrypted backup and the Room blob.
 */
object HavenJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false
        isLenient = false
    }
}

/**
 * Serializes/deserializes the [WorldState] JSON object and owns forward
 * migration. Deserialization funnels through [migrate] so an archive written by
 * an older [WorldState.CURRENT_SCHEMA_VERSION] is upgraded before use.
 */
class WorldSerializer(
    private val json: Json = HavenJson.instance,
) {
    fun encode(world: WorldState): String = json.encodeToString(WorldState.serializer(), world)

    fun decode(payload: String): WorldState {
        val decoded = json.decodeFromString(WorldState.serializer(), payload)
        return migrate(decoded)
    }

    /**
     * Applies stepwise upgrades until the world matches the current schema.
     * v1 is the initial schema, so this is currently a pass-through; future
     * breaking changes add `when (world.schemaVersion)` branches here.
     */
    private fun migrate(world: WorldState): WorldState = when (world.schemaVersion) {
        WorldState.CURRENT_SCHEMA_VERSION -> world
        else -> world.copy(schemaVersion = WorldState.CURRENT_SCHEMA_VERSION)
    }
}
