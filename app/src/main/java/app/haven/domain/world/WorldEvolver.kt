package app.haven.domain.world

import app.haven.data.model.HabitCategory
import app.haven.data.world.BotanicalGarden
import app.haven.data.world.Bridge
import app.haven.data.world.Dwelling
import app.haven.data.world.DwellingTier
import app.haven.data.world.EvolutionTier
import app.haven.data.world.Foliage
import app.haven.data.world.FoliageType
import app.haven.data.world.GridCoordinate
import app.haven.data.world.LibraryWing
import app.haven.data.world.PathSegment
import app.haven.data.world.PathSurface
import app.haven.data.world.ReadingRoom
import app.haven.data.world.StudyDesk
import app.haven.data.world.Tree
import app.haven.data.world.Villager
import app.haven.data.world.WaterFeature
import app.haven.data.world.WaterFeatureType
import app.haven.data.world.WorldState

/**
 * Translates claimed Growth Points into concrete diorama changes, implementing
 * the Habit-to-World graphics matrix (Section 3). Pure and deterministic: given
 * the same world + inputs it always yields the same next world, which is what
 * makes the whole evolution engine unit-testable without a device.
 *
 * A claim's GP is converted into a bounded number of incremental "growth ticks"
 * (more GP -> more ticks, capped) so a big claim visibly moves the world without
 * ever teleporting it.
 */
object WorldEvolver {

    private const val GP_PER_TICK = 60L
    private const val MAX_TICKS = 6

    fun evolve(
        world: WorldState,
        category: HabitCategory,
        growthPoints: Long,
        nowEpochMillis: Long,
    ): WorldState {
        val ticks = (growthPoints / GP_PER_TICK).toInt().coerceIn(1, MAX_TICKS)
        var next = world
        repeat(ticks) { next = applyTick(next, category) }
        return next.copy(lastEvolvedAtEpochMillis = nowEpochMillis)
    }

    private fun applyTick(world: WorldState, category: HabitCategory): WorldState =
        when (category) {
            HabitCategory.EXERCISE -> evolveInfrastructure(world)
            HabitCategory.READING -> evolveLibrary(world)
            HabitCategory.MEDITATION -> evolveBiodiversity(world)
            HabitCategory.CLEANING -> evolveResidential(world)
            HabitCategory.HYDRATION -> evolveWater(world)
            HabitCategory.SLEEP -> evolveAtmosphere(world)
            // Focus fuels GP generally; route it to infrastructure like exercise.
            HabitCategory.FOCUS -> evolveInfrastructure(world)
        }

    // -- EXERCISE -> paths, bridges, squares --------------------------------
    private fun evolveInfrastructure(world: WorldState): WorldState {
        val infra = world.infrastructure
        val upgradeIdx = infra.paths.indexOfFirst { it.surface != PathSurface.FLAGSTONE }
        val newInfra = if (upgradeIdx >= 0) {
            val seg = infra.paths[upgradeIdx]
            val paths = infra.paths.toMutableList().apply {
                this[upgradeIdx] = seg.copy(surface = nextSurface(seg.surface))
            }
            infra.copy(paths = paths)
        } else {
            val n = infra.paths.size
            val from = GridCoordinate(n.toFloat(), 0f)
            val to = GridCoordinate((n + 1).toFloat(), 0f)
            var updated = infra.copy(
                paths = infra.paths + PathSegment("path-$n", from, to, PathSurface.DIRT),
            )
            if ((n + 1) % 4 == 0) {
                updated = updated.copy(
                    bridges = updated.bridges + Bridge(
                        id = "bridge-${updated.bridges.size}",
                        at = to,
                        spanTiles = 2,
                        tier = EvolutionTier.EMERGING,
                    ),
                )
            }
            updated
        }
        return world.copy(infrastructure = newInfra)
    }

    // -- READING -> library wings, reading rooms, study desks ----------------
    private fun evolveLibrary(world: WorldState): WorldState {
        val d = world.intellectualDistrict
        val updated = when {
            d.wings.isEmpty() -> d.copy(
                wings = listOf(
                    LibraryWing("wing-0", "West Wing", GridCoordinate(0f, -2f), EvolutionTier.EMERGING),
                ),
            )
            d.wings.size < 3 -> d.copy(
                wings = d.wings + LibraryWing(
                    id = "wing-${d.wings.size}",
                    name = wingName(d.wings.size),
                    at = GridCoordinate(d.wings.size.toFloat(), -2f),
                    tier = EvolutionTier.EMERGING,
                ),
            )
            d.readingRooms.size < d.wings.size -> d.copy(
                readingRooms = d.readingRooms + ReadingRoom(
                    id = "room-${d.readingRooms.size}",
                    at = GridCoordinate(d.readingRooms.size.toFloat(), -3f),
                ),
            )
            else -> d.copy(
                studyDesks = d.studyDesks + StudyDesk(
                    id = "desk-${d.studyDesks.size}",
                    at = GridCoordinate(d.studyDesks.size.toFloat(), -1f),
                ),
            )
        }
        return world.copy(
            intellectualDistrict = updated.copy(
                libraryTier = advanceTier(updated.libraryTier),
                totalReadingMinutes = updated.totalReadingMinutes + 30,
            ),
        )
    }

    // -- MEDITATION -> foliage, trees, gardens, fireflies --------------------
    private fun evolveBiodiversity(world: WorldState): WorldState {
        val b = world.biodiversity
        val n = b.foliage.size
        var updated = b.copy(
            foliage = b.foliage + Foliage(
                id = "foliage-$n",
                at = GridCoordinate((n % 6 - 3).toFloat(), (n / 6 + 1).toFloat()),
                type = FoliageType.entries[n % FoliageType.entries.size],
            ),
            fireflies = b.fireflies.copy(
                active = true,
                density = (b.fireflies.density + 0.1f).coerceAtMost(1f),
            ),
        )
        if (n % 3 == 2) {
            updated = updated.copy(
                trees = updated.trees + Tree(
                    id = "tree-${updated.trees.size}",
                    at = GridCoordinate((updated.trees.size - 2).toFloat(), 2f),
                    tier = EvolutionTier.EMERGING,
                    ancient = updated.trees.size >= 5,
                ),
            )
        }
        if (n % 5 == 4) {
            updated = updated.copy(
                gardens = updated.gardens + BotanicalGarden(
                    id = "garden-${updated.gardens.size}",
                    at = GridCoordinate(2f, 2f),
                    radiusTiles = 1.5f,
                ),
            )
        }
        return world.copy(biodiversity = updated)
    }

    // -- CLEANING -> dwelling quality ---------------------------------------
    private fun evolveResidential(world: WorldState): WorldState {
        val r = world.residential
        val idx = r.dwellings.indexOfFirst { it.tier != DwellingTier.MID_CENTURY_MODERN }
        val updated = if (idx >= 0) {
            val dwelling = r.dwellings[idx]
            val dwellings = r.dwellings.toMutableList().apply {
                this[idx] = dwelling.copy(tier = nextDwelling(dwelling.tier))
            }
            r.copy(dwellings = dwellings)
        } else {
            val n = r.dwellings.size
            val villagerId = "villager-$n"
            r.copy(
                dwellings = r.dwellings + Dwelling(
                    id = "dwelling-$n",
                    at = GridCoordinate(n.toFloat(), n.toFloat()),
                    tier = DwellingTier.CANVAS_TENT,
                    occupantIds = listOf(villagerId),
                ),
                villagers = r.villagers + Villager(villagerId, "dwelling-$n"),
            )
        }
        return world.copy(residential = updated)
    }

    // -- HYDRATION -> water volume & feature type ---------------------------
    private fun evolveWater(world: WorldState): WorldState {
        val w = world.water
        val idx = w.features.indexOfFirst { it.type != WaterFeatureType.WATERFALL }
        val updated = if (idx >= 0) {
            val feature = w.features[idx]
            val nextType = nextWater(feature.type)
            val features = w.features.toMutableList().apply {
                this[idx] = feature.copy(
                    type = nextType,
                    volume = feature.volume + 1f,
                    flowing = nextType == WaterFeatureType.STREAM || nextType == WaterFeatureType.WATERFALL,
                )
            }
            w.copy(features = features, totalVolume = w.totalVolume + 1f)
        } else {
            val n = w.features.size
            w.copy(
                features = w.features + WaterFeature(
                    id = "water-$n",
                    at = GridCoordinate((n + 2).toFloat(), (n + 1).toFloat()),
                    type = WaterFeatureType.MUD_HOLE,
                ),
                totalVolume = w.totalVolume + 1f,
            )
        }
        return world.copy(water = updated)
    }

    // -- SLEEP -> fog clearance & sunrise clarity ---------------------------
    private fun evolveAtmosphere(world: WorldState): WorldState {
        val a = world.atmosphere
        return world.copy(
            atmosphere = a.copy(
                fog = a.fog.copy(density = (a.fog.density - 0.1f).coerceAtLeast(0f)),
                sunriseClarity = (a.sunriseClarity + 0.1f).coerceAtMost(1f),
            ),
        )
    }

    // -- Progression helpers -------------------------------------------------
    private fun nextSurface(s: PathSurface) = when (s) {
        PathSurface.DIRT -> PathSurface.GRAVEL
        PathSurface.GRAVEL -> PathSurface.PAVED
        PathSurface.PAVED -> PathSurface.FLAGSTONE
        PathSurface.FLAGSTONE -> PathSurface.FLAGSTONE
    }

    private fun nextDwelling(t: DwellingTier) = when (t) {
        DwellingTier.CANVAS_TENT -> DwellingTier.TIMBER_CABIN
        DwellingTier.TIMBER_CABIN -> DwellingTier.COTTAGE
        DwellingTier.COTTAGE -> DwellingTier.TOWNHOUSE
        DwellingTier.TOWNHOUSE -> DwellingTier.MID_CENTURY_MODERN
        DwellingTier.MID_CENTURY_MODERN -> DwellingTier.MID_CENTURY_MODERN
    }

    private fun nextWater(t: WaterFeatureType) = when (t) {
        WaterFeatureType.MUD_HOLE -> WaterFeatureType.POND
        WaterFeatureType.POND -> WaterFeatureType.STREAM
        WaterFeatureType.STREAM -> WaterFeatureType.WATERFALL
        WaterFeatureType.WATERFALL -> WaterFeatureType.WATERFALL
    }

    private fun advanceTier(t: EvolutionTier) = when (t) {
        EvolutionTier.SEED -> EvolutionTier.EMERGING
        EvolutionTier.EMERGING -> EvolutionTier.ESTABLISHED
        EvolutionTier.ESTABLISHED -> EvolutionTier.REFINED
        EvolutionTier.REFINED -> EvolutionTier.PREMIUM
        EvolutionTier.PREMIUM -> EvolutionTier.PREMIUM
    }

    private fun wingName(index: Int) = when (index) {
        1 -> "East Wing"
        2 -> "North Reading Hall"
        else -> "Annex $index"
    }
}
