package zelgius.com.budgetmanager.dao

import androidx.paging.DataSource
import androidx.room.*
import zelgius.com.budgetmanager.dao.AmountForPartCount.Companion.SQL
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
            SELECT p.*, CASE WHEN p.closed THEN
                            --(SELECT SUM(e.amount) * p1.percent FROM spare_entry e, budget_part p1 WHERE p1.id = p.id AND p1.close_date > e.date AND e.ref_budget =  :refBudget)
                            (SELECT SUM(a.amount) FROM AmountForPartCount a
                                JOIN spare_entry e ON e.id = entryId 
                            WHERE partId = p.id AND e.date < p.close_date)

                        WHEN NOT p.closed AND p.ref_budget IS NULL THEN
                            (SELECT SUM(e.amount) * p1.percent FROM spare_entry e, budget_part p1 WHERE p1.id = p.id AND e.ref_budget = :refBudget)
                        ELSE 
                            (SELECT SUM(amount) FROM AmountForPartCount WHERE partId = p.id)
                        END AS amount
            FROM budget_part p WHERE p.ref_budget =  :refBudget OR p.ref_budget IS NULL
            ORDER BY closed, ref_budget DESC,  name, close_date DESC
    """
    }
}


@DatabaseView(SQL)
data class AmountForPartCount(
        val amount: Double,
        val partId: Long,
        val entryId: Long
) {
    companion object {
        const val SQL = """
        SELECT e.id AS entryId, p.id AS partId ,  SUM(
		    e.amount  * (
	 		p.percent + COALESCE(
			 	(
			 		SELECT SUM(p1.percent) 
			 		FROM budget_part p1 
			 		WHERE e.date > p1.close_date AND p1.closed  AND (p1.ref_budget IS NULL OR p1.ref_budget = p.ref_budget)
			 	) / (
			 		SELECT COUNT(*) 
			 		FROM budget_part p1 
			 		WHERE p1.ref_budget = p.ref_budget AND (NOT p1.closed OR p1.close_date > e.date)
			 	)
		 	, 0))
        ) AS amount
        FROM budget_part p, spare_entry e GROUP BY p.id, e.id
        """
    }
}


data class BudgetPartWithAmount(
        @Embedded val part: BudgetPart,
        var amount: Double
) : Comparable<BudgetPartWithAmount> {
    override fun compareTo(other: BudgetPartWithAmount) =
            part.compareTo(other.part)

}


data class BudgetAndEntry(
        @Embedded(prefix = "b_") val budget: Budget?,
        @Embedded val entry: SpareEntry,
        val total: Double
)