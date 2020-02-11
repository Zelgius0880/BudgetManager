package zelgius.com.budgetmanager.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart

open class BudgetPartRepository(context: Context) {
    open val budgetPartDao by lazy { AppDatabase.getInstance(context).budgetPartDao }

    suspend fun insert(vararg budgetPart: BudgetPart) =
            withContext(Dispatchers.Default) {
                budgetPart.forEach {
                    with(budgetPartDao.insert(it)) {
                        it.id = this
                    }
                }
            }

    suspend fun update(vararg budgetPart: BudgetPart) =
            withContext(Dispatchers.Default) {
                budgetPart.forEach {
                    budgetPartDao.update(it)
                }
            }

    suspend fun delete(vararg budgetPart: BudgetPart) =
            withContext(Dispatchers.Default) {
                budgetPart.forEach {
                    budgetPartDao.delete(it)
                }
            }

    suspend fun get() =
            withContext(Dispatchers.Default) {
                budgetPartDao.get()
            }


    suspend fun get(refBudget: Long, ignoreClosed: Boolean  = false) =
            withContext(Dispatchers.Default) {
                if (!ignoreClosed)
                    budgetPartDao.get(refBudget)
                else {
                    val repartition = budgetPartDao.getRepartition(refBudget)
                    budgetPartDao.get(refBudget, repartition)
                }/*.sortedBy {
                    when {
                        it.closed -> 2
                        it.
                    }
                }*/
            }


    suspend fun getGreaterThanZero(refBudget: Long, ignoreClosed: Boolean  = false) =
            withContext(Dispatchers.Default) {
                if(!ignoreClosed)
                budgetPartDao.getGreaterThanZero(refBudget)
                else {
                    val repartition = budgetPartDao.getRepartition(refBudget)
                    budgetPartDao.getGreaterThanZero(refBudget, repartition)
                }
            }


    suspend fun getRepartition(budget: Budget) = budgetPartDao.getRepartition(budget.id!!)

    suspend fun getBudgetAndPart() =
            withContext(Dispatchers.Default) {
                budgetPartDao.getBudgetAndPart()
            }

    fun getDataSource() =
        budgetPartDao.getBudgetAndPartDataSource()

    fun getDataSource(budget: Budget) =
        budgetPartDao.getDataSource(budget.id!!)
}