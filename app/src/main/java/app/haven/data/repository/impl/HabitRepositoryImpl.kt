package app.haven.data.repository.impl

import app.haven.data.local.dao.HabitDao
import app.haven.data.local.entity.HabitLogEntity
import app.haven.data.model.DataSource
import app.haven.data.model.HabitCategory
import app.haven.data.repository.HabitLog
import app.haven.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
) : HabitRepository {

    override fun observeUnclaimed(): Flow<List<HabitLog>> =
        habitDao.observeUnclaimed().map { list -> list.map { it.toDomain() } }

    override fun observeHistory(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Flow<List<HabitLog>> =
        habitDao.observeBetween(startEpochMillis, endEpochMillis)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun log(
        category: HabitCategory,
        amount: Double,
        unit: String,
        source: DataSource,
        occurredAtEpochMillis: Long,
    ): Long = habitDao.insert(
        HabitLogEntity(
            category = category,
            amount = amount,
            unit = unit,
            source = source,
            occurredAtEpochMillis = occurredAtEpochMillis,
        ),
    )

    override suspend fun total(
        category: HabitCategory,
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Double = habitDao.sumAmount(category, startEpochMillis, endEpochMillis)

    private fun HabitLogEntity.toDomain() = HabitLog(
        id = id,
        category = category,
        amount = amount,
        unit = unit,
        source = source,
        occurredAtEpochMillis = occurredAtEpochMillis,
        claimed = claimed,
    )
}
