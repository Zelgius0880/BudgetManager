package zelgius.com.budgetmanager.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.budgetmanager.entities.Budget

open class BudgetRepository(context: Context) {
    open val budgetDao by lazy { AppDatabase.getInstance(context).budgetDao }

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

    suspend fun delete(vararg budget: Budget) =
            withContext(Dispatchers.Default) {
                budget.forEach {
                    budgetDao.delete(it)
                }
            }

    suspend fun get() =
            withContext(Dispatchers.Default) {
                budgetDao.get()
            }

    fun getDataSource() =
            budgetDao.getDataSource()
}