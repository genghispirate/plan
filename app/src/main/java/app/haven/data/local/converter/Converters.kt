package app.haven.data.local.converter

import androidx.room.TypeConverter
import app.haven.data.model.BackupStatus
import app.haven.data.model.BackupTargetType
import app.haven.data.model.ClaimStatus
import app.haven.data.model.DataSource
import app.haven.data.model.GateType
import app.haven.data.model.HabitCategory
import app.haven.data.model.RewardMetric

/**
 * Enum <-> String converters. Storing the enum *name* (not ordinal) keeps the
 * SQLite payload stable if the enum declaration order ever changes. Unknown
 * values fail loudly rather than corrupting silently.
 */
class Converters {

    @TypeConverter fun habitCategoryToString(v: HabitCategory): String = v.name
    @TypeConverter fun stringToHabitCategory(v: String): HabitCategory = HabitCategory.valueOf(v)

    @TypeConverter fun rewardMetricToString(v: RewardMetric): String = v.name
    @TypeConverter fun stringToRewardMetric(v: String): RewardMetric = RewardMetric.valueOf(v)

    @TypeConverter fun dataSourceToString(v: DataSource): String = v.name
    @TypeConverter fun stringToDataSource(v: String): DataSource = DataSource.valueOf(v)

    @TypeConverter fun claimStatusToString(v: ClaimStatus): String = v.name
    @TypeConverter fun stringToClaimStatus(v: String): ClaimStatus = ClaimStatus.valueOf(v)

    @TypeConverter fun gateTypeToString(v: GateType): String = v.name
    @TypeConverter fun stringToGateType(v: String): GateType = GateType.valueOf(v)

    @TypeConverter fun backupTargetToString(v: BackupTargetType): String = v.name
    @TypeConverter fun stringToBackupTarget(v: String): BackupTargetType = BackupTargetType.valueOf(v)

    @TypeConverter fun backupStatusToString(v: BackupStatus): String = v.name
    @TypeConverter fun stringToBackupStatus(v: String): BackupStatus = BackupStatus.valueOf(v)
}
