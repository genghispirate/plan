package app.haven.data.repository.impl

import app.haven.data.local.dao.OnboardingDao
import app.haven.data.local.entity.OnboardingChoiceEntity
import app.haven.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class OnboardingRepositoryImpl(
    private val onboardingDao: OnboardingDao,
) : OnboardingRepository {

    override fun observeChoices(): Flow<List<OnboardingChoiceEntity>> =
        onboardingDao.observeAll()

    override suspend fun commitChoice(key: String, value: String, dayIndex: Int) {
        onboardingDao.upsert(
            OnboardingChoiceEntity(
                choiceKey = key,
                choiceValue = value,
                dayIndex = dayIndex,
                // Endowment-effect choices are permanent the moment they're made.
                locked = true,
                committedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    /** All three onboarding rituals complete once three choices are locked. */
    override suspend fun isComplete(): Boolean = onboardingDao.lockedCount() >= REQUIRED_CHOICES

    private companion object { const val REQUIRED_CHOICES = 3 }
}
