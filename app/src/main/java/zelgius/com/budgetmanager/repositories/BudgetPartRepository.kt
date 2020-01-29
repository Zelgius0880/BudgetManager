package zelgius.com.budgetmanager.repositories

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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


    suspend fun get(refBudget: Long) =
            withContext(Dispatchers.Default) {
                budgetPartDao.get(refBudget)
            }


    suspend fun getGreaterThanZero(refBudget: Long) =
            withContext(Dispatchers.Default) {
                budgetPartDao.getGreaterThanZero(refBudget)
            }


    suspend fun getBudgetAndPart() =
            withContext(Dispatchers.Default) {
                budgetPartDao.getBudgetAndPart()
            }

    fun getDataSource() =
        budgetPartDao.getBudgetAndPartDataSource()
}