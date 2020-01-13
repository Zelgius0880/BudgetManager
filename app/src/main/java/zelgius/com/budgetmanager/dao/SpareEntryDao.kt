package zelgius.com.budgetmanager.dao

import androidx.paging.DataSource
import androidx.room.*
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.SpareEntry

@Dao
interface SpareEntryDao {
    @Insert
    suspend fun insert(spareEntry: SpareEntry): Long

    @Update
    suspend fun update(spareEntry: SpareEntry): Int

    @Delete
    suspend fun delete(spareEntry: SpareEntry): Int

    @Query("SELECT * FROM spare_entry")
    suspend fun get(): List<SpareEntry>

    @Query("""
       SELECT b.id AS b_id, b.name AS b_name, b.closed AS b_closed, b.start_date AS b_start_date, e.* FROM spare_entry e
        JOIN budget b ON e.ref_budget = b.id
        ORDER BY b.closed, b.start_date, e.ref_budget
    """)
    suspend fun getBudgetAndEntry(): List<BudgetAndEntry>

    @Query("""
       SELECT b.id AS b_id, b.name AS b_name, b.closed AS b_closed, b.start_date AS b_start_date, e.* FROM spare_entry e
        JOIN budget b ON e.ref_budget = b.id
        ORDER BY b.closed, b.start_date, e.ref_budget
    """)
    fun getBudgetAndEntryDataSource(): DataSource.Factory<Int, BudgetAndEntry>


}

data class BudgetAndEntry(
        @Embedded(prefix = "b_")  val budget: Budget,
        @Embedded val entry: SpareEntry
)