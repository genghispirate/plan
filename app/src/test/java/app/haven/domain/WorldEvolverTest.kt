package app.haven.domain

import app.haven.data.model.HabitCategory
import app.haven.data.world.DwellingTier
import app.haven.data.world.PathSurface
import app.haven.data.world.WaterFeatureType
import app.haven.data.world.WorldState
import app.haven.domain.world.WorldEvolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldEvolverTest {

    private fun newWorld() = WorldState.newWorld("world-test", seed = 1L, nowEpochMillis = 0L)

    @Test fun `exercise lays the first path`() {
        val evolved = WorldEvolver.evolve(newWorld(), HabitCategory.EXERCISE, growthPoints = 60, nowEpochMillis = 10L)
        assertEquals(1, evolved.infrastructure.paths.size)
        assertEquals(PathSurface.DIRT, evolved.infrastructure.paths.first().surface)
        assertEquals(10L, evolved.lastEvolvedAtEpochMillis)
    }

    @Test fun `larger claims apply more growth ticks and upgrade surfaces`() {
        // 300 GP -> 5 ticks: add dirt, then upgrade dirt->gravel->paved->flagstone,
        // then add a second dirt path.
        val evolved = WorldEvolver.evolve(newWorld(), HabitCategory.EXERCISE, growthPoints = 300, nowEpochMillis = 0L)
        assertEquals(2, evolved.infrastructure.paths.size)
        assertEquals(PathSurface.FLAGSTONE, evolved.infrastructure.paths.first().surface)
    }

    @Test fun `hydration expands the water body`() {
        val evolved = WorldEvolver.evolve(newWorld(), HabitCategory.HYDRATION, growthPoints = 60, nowEpochMillis = 0L)
        assertEquals(WaterFeatureType.POND, evolved.water.features.first().type)
        assertEquals(1f, evolved.water.totalVolume, 0.001f)
    }

    @Test fun `cleaning upgrades a dwelling one tier`() {
        val evolved = WorldEvolver.evolve(newWorld(), HabitCategory.CLEANING, growthPoints = 60, nowEpochMillis = 0L)
        assertEquals(DwellingTier.TIMBER_CABIN, evolved.residential.dwellings.first().tier)
    }

    @Test fun `meditation activates fireflies and grows foliage`() {
        val evolved = WorldEvolver.evolve(newWorld(), HabitCategory.MEDITATION, growthPoints = 60, nowEpochMillis = 0L)
        assertTrue(evolved.biodiversity.fireflies.active)
        assertTrue(evolved.biodiversity.fireflies.density > 0f)
        assertEquals(1, evolved.biodiversity.foliage.size)
    }

    @Test fun `reading raises the first library wing`() {
        val evolved = WorldEvolver.evolve(newWorld(), HabitCategory.READING, growthPoints = 60, nowEpochMillis = 0L)
        assertEquals(1, evolved.intellectualDistrict.wings.size)
        assertEquals(30L, evolved.intellectualDistrict.totalReadingMinutes)
    }

    @Test fun `sleep clears fog and sharpens sunrise`() {
        val evolved = WorldEvolver.evolve(newWorld(), HabitCategory.SLEEP, growthPoints = 60, nowEpochMillis = 0L)
        assertEquals(0.9f, evolved.atmosphere.fog.density, 0.001f)
        assertEquals(0.6f, evolved.atmosphere.sunriseClarity, 0.001f)
    }
}
