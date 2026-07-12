package app.haven.domain

import app.haven.data.world.RestTrigger
import app.haven.data.world.WeatherPhase
import app.haven.data.world.WorldState
import app.haven.domain.world.RestStateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestStateManagerTest {

    private fun newWorld() = WorldState.newWorld("w", seed = 1L, nowEpochMillis = 0L)

    @Test fun `severe overuse brings rain and moves villagers indoors`() {
        val rested = RestStateManager.enterRest(newWorld(), RestTrigger.APP_OVERUSE, nowEpochMillis = 5L)
        assertTrue(rested.restState.isResting)
        assertEquals(WeatherPhase.RAINY, rested.weather)
        assertTrue(rested.residential.villagers.all { it.indoors })
        assertTrue(rested.residential.dwellings.none { it.chimneySmoke })
    }

    @Test fun `a mild failure only clouds over`() {
        val rested = RestStateManager.enterRest(newWorld(), RestTrigger.FOCUS_SESSION_FAILED, nowEpochMillis = 0L)
        assertEquals(WeatherPhase.OVERCAST, rested.weather)
    }

    @Test fun `completing a habit clears rest back to sunshine`() {
        val rested = RestStateManager.enterRest(newWorld(), RestTrigger.APP_OVERUSE, nowEpochMillis = 0L)
        val cleared = RestStateManager.clearRest(rested, nowEpochMillis = 100L)
        assertFalse(cleared.restState.isResting)
        assertEquals(WeatherPhase.SUNSHINE, cleared.weather)
        assertTrue(cleared.residential.villagers.none { it.indoors })
        assertTrue(cleared.residential.dwellings.all { it.chimneySmoke })
    }

    @Test fun `nothing is destroyed entering rest`() {
        val world = newWorld()
        val rested = RestStateManager.enterRest(world, RestTrigger.STREAK_BROKEN, nowEpochMillis = 0L)
        // Structures and villagers persist — only mood/behaviour changes.
        assertEquals(world.residential.dwellings.size, rested.residential.dwellings.size)
        assertEquals(world.residential.villagers.size, rested.residential.villagers.size)
    }
}
