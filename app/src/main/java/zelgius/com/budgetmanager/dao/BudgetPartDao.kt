package zelgius.com.budgetmanager.dao

import androidx.paging.DataSource
import androidx.room.*
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart

@Dao
interface BudgetPartDao {
    @Insert
    suspend fun insert(budgetPart: BudgetPart): Long

    @Update
    suspend fun update(budgetPart: BudgetPart): Int

    @Delete
    suspend fun delete(budgetPart: BudgetPart): Int

    @Query("SELECT * FROM budget_part")
    suspend fun get(): List<BudgetPart>

    @Query("""
       SELECT b.id AS b_id, b.name AS b_name, b.closed AS b_closed, b.start_date AS b_start_date, p.* FROM budget_part p
        JOIN budget b ON p.ref_budget = b.id
        ORDER BY b.closed, b.start_date, p.ref_budget
    """)
    suspend fun getBudgetAndPart(): List<BudgetAndPart>

    @Query("""
       SELECT b.id AS b_id, b.name AS b_name, b.closed AS b_closed, b.start_date AS b_start_date, p.* FROM budget_part p
        JOIN budget b ON p.ref_budget = b.id
        ORDER BY b.closed, b.start_date, p.ref_budget
    """)
    fun getBudgetAndPartDataSource(): DataSource.Factory<Int, BudgetAndPart>


}

data class BudgetAndPart(
        @Embedded(prefix = "b_")  val budget: Budget,
        @Embedded val part: BudgetPart
)