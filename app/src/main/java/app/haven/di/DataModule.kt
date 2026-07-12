package app.haven.di

import android.content.Context
import androidx.room.Room
import app.haven.data.backup.BackupCrypto
import app.haven.data.backup.LocalSerializationManager
import app.haven.data.backup.LocalSerializationManagerImpl
import app.haven.data.local.HavenDatabase
import app.haven.data.local.dao.BackupDao
import app.haven.data.local.dao.BlockedAppDao
import app.haven.data.local.dao.ClaimDao
import app.haven.data.local.dao.EconomyDao
import app.haven.data.local.dao.HabitDao
import app.haven.data.local.dao.OnboardingDao
import app.haven.data.local.dao.StreakDao
import app.haven.data.local.dao.TimelapseDao
import app.haven.data.local.dao.WorldDao
import app.haven.data.repository.BackupRepository
import app.haven.data.repository.ClaimRepository
import app.haven.data.repository.EconomyRepository
import app.haven.data.repository.HabitRepository
import app.haven.data.repository.OnboardingRepository
import app.haven.data.repository.WorldRepository
import app.haven.data.repository.impl.BackupRepositoryImpl
import app.haven.data.repository.impl.ClaimRepositoryImpl
import app.haven.data.repository.impl.EconomyRepositoryImpl
import app.haven.data.repository.impl.HabitRepositoryImpl
import app.haven.data.repository.impl.OnboardingRepositoryImpl
import app.haven.data.repository.impl.WorldRepositoryImpl
import app.haven.data.serialization.HavenJson
import app.haven.data.serialization.WorldSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Constructs and wires the offline data layer. Everything is a singleton — one
 * database, one serializer, one repository of each kind — so the world is a
 * single source of truth across the app.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // ---- Persistence ------------------------------------------------------

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HavenDatabase =
        Room.databaseBuilder(context, HavenDatabase::class.java, HavenDatabase.DATABASE_NAME)
            // WAL keeps reads non-blocking against the render observer; explicit
            // so the backup checkpoint behaviour is deterministic.
            .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides fun provideWorldDao(db: HavenDatabase): WorldDao = db.worldDao()
    @Provides fun provideHabitDao(db: HavenDatabase): HabitDao = db.habitDao()
    @Provides fun provideClaimDao(db: HavenDatabase): ClaimDao = db.claimDao()
    @Provides fun provideEconomyDao(db: HavenDatabase): EconomyDao = db.economyDao()
    @Provides fun provideStreakDao(db: HavenDatabase): StreakDao = db.streakDao()
    @Provides fun provideOnboardingDao(db: HavenDatabase): OnboardingDao = db.onboardingDao()
    @Provides fun provideBlockedAppDao(db: HavenDatabase): BlockedAppDao = db.blockedAppDao()
    @Provides fun provideBackupDao(db: HavenDatabase): BackupDao = db.backupDao()
    @Provides fun provideTimelapseDao(db: HavenDatabase): TimelapseDao = db.timelapseDao()

    // ---- Serialization ----------------------------------------------------

    @Provides @Singleton fun provideJson(): Json = HavenJson.instance

    @Provides @Singleton fun provideWorldSerializer(json: Json): WorldSerializer =
        WorldSerializer(json)

    // ---- Backup pipeline --------------------------------------------------

    @Provides @Singleton fun provideBackupCrypto(): BackupCrypto = BackupCrypto()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideSerializationManager(
        @ApplicationContext context: Context,
        crypto: BackupCrypto,
    ): LocalSerializationManager = LocalSerializationManagerImpl(context, crypto)

    // ---- Repositories -----------------------------------------------------

    @Provides
    @Singleton
    fun provideWorldRepository(
        worldDao: WorldDao,
        serializer: WorldSerializer,
    ): WorldRepository = WorldRepositoryImpl(worldDao, serializer)

    @Provides
    @Singleton
    fun provideHabitRepository(habitDao: HabitDao): HabitRepository =
        HabitRepositoryImpl(habitDao)

    @Provides
    @Singleton
    fun provideClaimRepository(db: HavenDatabase): ClaimRepository =
        ClaimRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideEconomyRepository(db: HavenDatabase): EconomyRepository =
        EconomyRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideOnboardingRepository(onboardingDao: OnboardingDao): OnboardingRepository =
        OnboardingRepositoryImpl(onboardingDao)

    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        db: HavenDatabase,
        backupDao: BackupDao,
        worldRepository: WorldRepository,
        serializer: WorldSerializer,
        serializationManager: LocalSerializationManager,
    ): BackupRepository = BackupRepositoryImpl(
        context = context,
        db = db,
        backupDao = backupDao,
        worldRepository = worldRepository,
        serializer = serializer,
        serializationManager = serializationManager,
    )
}
