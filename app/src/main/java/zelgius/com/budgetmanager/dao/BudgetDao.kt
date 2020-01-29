package zelgius.com.budgetmanager.dao

import androidx.paging.DataSource
import androidx.room.*
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart

@Dao
interface BudgetDao {
    @Insert
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(vararg budget: Budget): Int

    @Delete
    suspend fun delete(vararg budget: Budget): Int

    @Query("SELECT * FROM budget")
    suspend fun get(): List<Budget>

    @Query("SELECT * FROM budget WHERE id = :id")
    suspend fun get(id: Long): Budget?

    @Query("SELECT * FROM budget WHERE closed = 0")
    suspend fun getNotClosed(): List<Budget>

    @Query("SELECT * FROM budget WHERE closed = 1")
    suspend fun getClosed(): List<Budget>

    @Query("SELECT * FROM budget")
    fun getDataSource(): DataSource.Factory<Int, Budget>


    @Query("UPDATE budget_part SET closed = :closed WHERE ref_budget = :refBudget")
    fun closePartFromBudget(closed: Boolean, refBudget: Long)

}