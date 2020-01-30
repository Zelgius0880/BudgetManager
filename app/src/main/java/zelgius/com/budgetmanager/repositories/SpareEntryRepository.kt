package zelgius.com.budgetmanager.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.budgetmanager.dao.BudgetPartWithAmount
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.SpareEntry

open class SpareEntryRepository(context: Context) {
    open val spareEntryDao by lazy { AppDatabase.getInstance(context).spareEntryDao }

    suspend fun insert(vararg spareEntry: SpareEntry) =
            withContext(Dispatchers.Default) {
                spareEntry.forEach {
                    with(spareEntryDao.insert(it)) {
                        it.id = this
                    }
                }
            }

    suspend fun update(vararg spareEntry: SpareEntry) =
            withContext(Dispatchers.Default) {
                spareEntry.forEach {
                    spareEntryDao.update(it)
                }
            }

    suspend fun delete(vararg spareEntry: SpareEntry) =
            withContext(Dispatchers.Default) {
                spareEntry.forEach {
                    spareEntryDao.delete(it)
                }
            }

    suspend fun get() =
            withContext(Dispatchers.Default) {
                spareEntryDao.get()
            }

    suspend fun getByBudget(budget: Budget) =
            spareEntryDao.getByBudget(budget.id!!)

    suspend fun getSum(budget: Budget) =
            spareEntryDao.getSum(budget.id!!)



    fun getDataSource() =
            spareEntryDao.getDataSource()

    fun getBudgetAndEntryDataSource() =
            spareEntryDao.getBudgetAndEntryDataSource()

    suspend fun getBudgetPartWithAmount(budget: Budget) =
            spareEntryDao.getBudgetPartWithAmount(budget.id!!,
                    spareEntryDao.getRepartition(budget.id!!)
            )
}