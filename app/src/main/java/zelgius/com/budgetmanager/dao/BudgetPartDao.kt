package zelgius.com.budgetmanager.dao

import androidx.paging.DataSource
import androidx.room.*
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.entities.SpareEntry

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

    @Query(GET_QUERY)
    suspend fun get(refBudget: Long): List<BudgetPart>

    @Query(GET_QUERY)
    fun getDataSource(refBudget: Long): DataSource.Factory<Int, BudgetPart>

    @Query("""SELECT id, name, goal, closed, reached, close_date, ref_budget,
        CASE WHEN ref_budget IS NOT NULL THEN percent + :repartition ELSE percent END AS percent FROM budget_part 
        WHERE (ref_budget = :refBudget OR ref_budget IS NULL) AND NOT closed
        ORDER BY  closed, ref_budget DESC, name ASC """)
    suspend fun get(refBudget: Long, repartition: Double): List<BudgetPart>


    @Query("""SELECT * FROM budget_part 
        WHERE ref_budget = :refBudget OR ref_budget IS NULL
        ORDER BY  closed, ref_budget DESC, name ASC """)
    suspend fun getForGraph(refBudget: Long): List<BudgetPart>

    @Query("""
       SELECT b.id AS b_id, b.name AS b_name, b.closed AS b_closed, b.start_date AS b_start_date, p.* FROM budget b
        LEFT OUTER JOIN budget_part p ON p.ref_budget = b.id
        ORDER BY b.closed, b.start_date
    """)
    suspend fun getBudgetAndPart(): List<BudgetAndPart>

    @Query("""
        SELECT b.id AS b_id, b.name AS b_name, b.closed AS b_closed, b.start_date AS b_start_date, p.* FROM budget b
        LEFT OUTER JOIN budget_part p ON p.ref_budget = b.id
        ORDER BY b.closed, b.start_date
    """)
    fun getBudgetAndPartDataSource(): DataSource.Factory<Int, BudgetAndPart>

    @Query("""
        SELECT (1 - SUM(percent)) / (SELECT COUNT(*) FROM budget_part WHERE NOT closed AND (ref_budget = :refBudget)) FROM budget_part 
        WHERE NOT closed AND (ref_budget = :refBudget OR ref_budget IS NULL)  
    """)
    suspend fun getRepartition(refBudget: Long): Double?

    companion object {
        const val GET_QUERY = "SELECT * FROM budget_part WHERE ref_budget = :refBudget OR ref_budget IS NULL ORDER BY  closed, ref_budget DESC, name ASC"
    }
}

data class BudgetAndPart(
        @Embedded(prefix = "b_")  val budget: Budget,
        @Embedded val part: BudgetPart?
)