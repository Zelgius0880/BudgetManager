package zelgius.com.budgetmanager.repositories

import zelgius.com.budgetmanager.dao.BudgetDao
import zelgius.com.budgetmanager.dao.BudgetPartDao
import zelgius.com.budgetmanager.dao.SpareEntryDao
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.entities.SpareEntry
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
        entities = [Budget::class, BudgetPart::class, SpareEntry::class],
        version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val budgetPartDao: BudgetPartDao
    abstract val budgetDao: BudgetDao
    abstract val spareEntryDao: SpareEntryDao

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context, test: Boolean = false): AppDatabase {
            if (instance == null) {

                instance = if (!test)
                    Room.databaseBuilder(
                            context,
                            AppDatabase::class.java, "database"
                    )
                            //.fallbackToDestructiveMigration()
                            .addCallback(object : Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                }
                            })
                            .build()
                else
                    Room.inMemoryDatabaseBuilder(
                            context, AppDatabase::class.java).build()
            }

            return instance!!
        }

        /*val MIGRATION_1_2 = createMigration(1, 2){
            it.execSQL("")
        }*/
        private fun createMigration(from: Int, to: Int, work: (SupportSQLiteDatabase) -> Unit) =
                object : Migration(from, to) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        work(database)
                    }
                }
    }
}

class Converters {


    @TypeConverter
    fun localDateTimeToLong(date: LocalDateTime): Long = date.toInstant(ZoneOffset.UTC).toEpochMilli()

    @TypeConverter
    fun longToLocalDateTime(stamp: Long): LocalDateTime = Instant.ofEpochMilli(stamp).atZone(ZoneOffset.UTC).toLocalDateTime();
}