package zelgius.com.budgetmanager.repositories

import android.content.Context
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.room.InvalidationTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zelgius.com.budgetmanager.dao.BudgetPartWithAmount
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.SpareEntry
import java.time.LocalDateTime

open class SpareEntryRepository(val context: Context) {
    private var factory: DataSource.Factory<Int, BudgetPartWithAmount>?= null
    open val spareEntryDao by lazy { AppDatabase.getInstance(context).spareEntryDao }
    open val budgetPartDao by lazy { AppDatabase.getInstance(context).budgetPartDao }

    suspend fun insert(vararg spareEntry: SpareEntry) {
        withContext(Dispatchers.Default) {

            spareEntry.forEach {
                with(spareEntryDao.insert(it)) {
                    it.id = this
                }
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
            spareEntryDao.getBudgetPartWithAmount(budget.id!!)

    fun getPartAndAmountDataSource(budget: Budget) = spareEntryDao.getBudgetPartWithAmountDataSource(budget.id!!)

    /*suspend fun getPartAndAmountDataSource(budget: Budget) =
            withContext(Dispatchers.Default) {
                val entries = spareEntryDao.getByBudget(budget.id!!)
                Factory(budget, context)
                        .mapByPage { parts ->
                            parts.forEach { it.amount = 0.0 }
                            entries.forEach { e ->
                                var totalPct = 0.0
                                parts.filter{!it.part.closed || (it.part.closeDate?: LocalDateTime.now()).isAfter(e.date)}
                                .forEach {
                                    it.amount += e.amount * it.part.percent
                                    totalPct += it.part.percent
                                }

                                if(totalPct < 1.0) {
                                    parts.filter { !it.part.closed && it.part.refBudget != null }
                                            .apply {
                                                forEach {
                                                    it.amount += e.amount * (1-totalPct) / size
                                                }
                                            }
                                }
                            }

                            parts.sortedBy{it.part}
                        }
            }*/

    inner class Factory(val budget: Budget, context: Context) : DataSource.Factory<Int, BudgetPartWithAmount>() {
        private var observer: DataSourceTableObserver? = DataSourceTableObserver("spare_entry")
        private val tracker: InvalidationTracker = AppDatabase.getInstance(context).invalidationTracker
        override fun create(): DataSource<Int, BudgetPartWithAmount> {
            val dataSource = spareEntryDao
                    .getBudgetPartWithAmountDataSource(budget.id!!).create()
            observer?.setCurrentDataSource(dataSource)
            return dataSource
        }

        fun cleanUp() {
            if(observer != null)
                tracker.removeObserver(observer!!)
            observer = null
        }

        init {
            if(observer != null)
                tracker.addObserver(observer!!)
        }
    }


    class DataSourceTableObserver(tableName: String) : InvalidationTracker.Observer(tableName) {
        private var dataSource: DataSource<*, *>? = null
        override fun onInvalidated(tables: Set<String>) {
            if (dataSource != null) dataSource!!.invalidate()
        }

        fun setCurrentDataSource(source: DataSource<*, *>?) {
            dataSource = source
        }
    }
}

