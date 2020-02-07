package zelgius.com.budgetmanager.dao

import androidx.paging.DataSource
import androidx.room.*
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
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
        SELECT * FROM spare_entry 
        WHERE ref_budget = :id
    """)
    suspend fun getByBudget(id: Long): List<SpareEntry>

    @Query("""
        SELECT SUM(amount) FROM spare_entry 
        WHERE ref_budget = :id
    """)
    suspend fun getSum(id: Long): Double

    @Query("SELECT * FROM spare_entry")
    fun getDataSource(): DataSource.Factory<Int, SpareEntry>


    @Query(BUDGET_PART_WITH_AMOUNT_QUERY)
    suspend fun getBudgetPartWithAmount(refBudget: Long): List<BudgetPartWithAmount>


    @Query(BUDGET_PART_WITH_AMOUNT_QUERY)
    fun getBudgetPartWithAmountDataSource(refBudget: Long): DataSource.Factory<Int, BudgetPartWithAmount>

    @Query("UPDATE spare_entry SET ref_budget = NULL WHERE ref_budget = :refBudget")
    suspend fun updateRefBudgetToNull(refBudget: Long)

    @Query("""
        SELECT b.id AS b_id, b.name AS b_name, b.closed AS b_closed, b.start_date AS b_start_date, e.*, 
        (SELECT SUM(e1.amount) FROM spare_entry e1 WHERE e1.ref_budget = b.id OR b.id IS NULL AND e1.ref_budget IS NULL ) as total
        FROM spare_entry e
        LEFT OUTER JOIN budget b ON e.ref_budget = b.id
        ORDER BY e.date, b_start_date, b_closed 
    """)
    fun getBudgetAndEntryDataSource(): DataSource.Factory<Int, BudgetAndEntry>

    companion object {
        const val BUDGET_PART_WITH_AMOUNT_QUERY = """
            SELECT * FROM (
                WITH RECURSIVE repartition(r) AS (
                    SELECT (1 - SUM(percent)) / COUNT(*) FROM budget_part 
                    WHERE NOT closed AND ref_budget = :refBudget
                )
              
                SELECT p.*, r, 
                        (
                            SELECT SUM(e.amount) * (p.percent + CASE WHEN NOT p.closed THEN r ELSE 0 END)
                            FROM spare_entry e
                            WHERE e.ref_budget = :refBudget AND (NOT p.closed OR p.close_date > e.date)
                        ) AS part_amount
                FROM budget_part p, repartition
                WHERE p.ref_budget = :refBudget AND (p.percent + CASE WHEN NOT p.closed THEN r ELSE 0 END) > 0
                ORDER BY closed, close_date DESC
        )
    """
    }
}


data class BudgetPartWithAmount(
        @Embedded val part: BudgetPart,
        @ColumnInfo(name = "part_amount") val amount: Double
)


data class BudgetAndEntry(
        @Embedded(prefix = "b_") val budget: Budget?,
        @Embedded val entry: SpareEntry,
        val total: Double
)