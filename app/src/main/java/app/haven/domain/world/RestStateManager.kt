package app.haven.domain.world

import app.haven.data.world.RestState
import app.haven.data.world.RestTrigger
import app.haven.data.world.WeatherPhase
import app.haven.data.world.WorldState

/**
 * The "Absence of Guilt" Rest State (Section 1.1). Failure never destroys — it
 * shifts the world to a safe, overcast stagnation: villagers move indoors,
 * chimney smoke stops, construction pauses. Completing any real habit clears it
 * back to warm sunshine. Pure transitions on [WorldState].
 */
object RestStateManager {

    /** Enter rest. Severity picks overcast vs. full rain, but nothing is lost. */
    fun enterRest(world: WorldState, trigger: RestTrigger, nowEpochMillis: Long): WorldState {
        if (world.restState.isResting) return world
        val intensity = intensityFor(trigger)
        val weather = if (intensity >= RAIN_THRESHOLD) WeatherPhase.RAINY else WeatherPhase.OVERCAST
        return world.copy(
            weather = weather,
            restState = RestState(
                isResting = true,
                trigger = trigger,
                enteredAtEpochMillis = nowEpochMillis,
                intensity = intensity,
            ),
            residential = world.residential.copy(
                villagers = world.residential.villagers.map { it.copy(indoors = true) },
                dwellings = world.residential.dwellings.map { it.copy(chimneySmoke = false) },
            ),
        )
    }

    /** Clear rest — completing a habit immediately returns the world to sunshine. */
    fun clearRest(world: WorldState, nowEpochMillis: Long): WorldState {
        if (!world.restState.isResting && world.weather == WeatherPhase.SUNSHINE) return world
        return world.copy(
            weather = WeatherPhase.SUNSHINE,
            restState = RestState(isResting = false),
            residential = world.residential.copy(
                villagers = world.residential.villagers.map { it.copy(indoors = false) },
                dwellings = world.residential.dwellings.map { it.copy(chimneySmoke = true) },
            ),
            lastEvolvedAtEpochMillis = nowEpochMillis,
        )
    }

    private fun intensityFor(trigger: RestTrigger): Float = when (trigger) {
        RestTrigger.NONE -> 0f
        RestTrigger.FOCUS_SESSION_FAILED -> 0.5f
        RestTrigger.STREAK_BROKEN -> 0.7f
        RestTrigger.APP_OVERUSE -> 0.9f
    }

    private const val RAIN_THRESHOLD = 0.7f
}
