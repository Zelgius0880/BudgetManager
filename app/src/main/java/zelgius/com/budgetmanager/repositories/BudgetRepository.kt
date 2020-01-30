package zelgius.com.budgetmanager.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.budgetmanager.entities.Budget

open class BudgetRepository(context: Context) {
    open val budgetDao by lazy { AppDatabase.getInstance(context).budgetDao }
    open val spareEntryDao by lazy { AppDatabase.getInstance(context).spareEntryDao }

    suspend fun insert(vararg budget: Budget) =
            withContext(Dispatchers.Default) {
                budget.forEach {
                    with(budgetDao.insert(it)) {
                        it.id = this
                    }
                }
            }

    suspend fun update(vararg budget: Budget) =
            withContext(Dispatchers.Default) {
                budget.forEach {
                    budgetDao.update(it)
                }
            }

    suspend fun closeBudget(closed: Boolean, budget: Budget) =
            withContext(Dispatchers.Default) {
                budgetDao.closePartFromBudget(closed, budget.id!!)
            }

    suspend fun delete(vararg budget: Budget) =
            withContext(Dispatchers.Default) {
                budget.forEach {
                    spareEntryDao.updateRefBudgetToNull(it.id!!)
                    budgetDao.delete(it)
                }
            }

    suspend fun get() =
            withContext(Dispatchers.Default) {
                budgetDao.get()
            }

    suspend fun get(id: Long) =
            withContext(Dispatchers.Default) {
                budgetDao.get(id)
            }


    suspend fun get(closed: Boolean) =
            withContext(Dispatchers.Default) {
                if(closed) budgetDao.getClosed()
                else budgetDao.getNotClosed()
            }

    fun getDataSource() =
            budgetDao.getDataSource()
}