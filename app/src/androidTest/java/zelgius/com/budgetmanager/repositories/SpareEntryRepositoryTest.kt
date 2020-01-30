package zelgius.com.budgetmanager.repositories

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.entities.SpareEntry
import java.time.LocalDateTime

class SpareEntryRepositoryTest {

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = SpareEntryRepositoryTestImpl(context)
        repositoryBudget = BudgetRepositoryTest.Companion.BudgetRepositoryTestImpl(context)
        repositoryBudgetPart = BudgetPartRepositoryTest.Companion.BudgetPartRepositoryTestImpl(context)
    }

    companion object {

        class SpareEntryRepositoryTestImpl(private val context: Context) : SpareEntryRepository(context) {
            override val spareEntryDao by lazy { AppDatabase.getInstance(context, true).spareEntryDao }

            fun close() = AppDatabase.getInstance(context, true).close()
        }

        private lateinit var repository: SpareEntryRepositoryTestImpl
        private lateinit var repositoryBudget: BudgetRepositoryTest.Companion.BudgetRepositoryTestImpl
        private lateinit var repositoryBudgetPart: BudgetPartRepositoryTest.Companion.BudgetPartRepositoryTestImpl

        @JvmStatic
        @AfterClass
        fun closeDb() {
            repository.close()
        }
    }


    @Test
    fun insert() {
        val e = SpareEntry(comment = "Budget Test")

        runBlocking {
            repository.insert(e)
            val list = repository.get()

            assertTrue(list.isNotEmpty())
            assertTrue(list.contains(e))
        }

    }

    @Test
    fun update() {
        val e = SpareEntry(comment = "Budget Test")

        runBlocking {
            repository.insert(e)
            assertTrue(repository.get().contains(e))

            e.comment += " test"
            repository.update(e)
            assertTrue(repository.get().contains(e))
        }

    }

    @Test
    fun delete() {
        val b = SpareEntry(comment = "Budget Test")

        runBlocking {
            repository.insert(b)
            assertTrue(repository.get().contains(b))

            repository.delete(b)
            assertFalse(repository.get().contains(b))
        }
    }


    data class SampleResult(val budgets: List<Budget>, val list: MutableList<SpareEntry>, val b: Budget)

    suspend fun insertSample(samples: Int = 5): SampleResult {
        val budgets = listOf(Budget(name = "Budget Test 1"), Budget(name = "Budget Test 2"), Budget(name = "Budget Test 3"))
        val b = budgets[1]

        repositoryBudget.insert(*budgets.toTypedArray())
        assertTrue(repositoryBudget.get().contains(b))

        val list = (1..samples).map {
            SpareEntry(
                    comment = "Part $it",
                    amount = 100.0 * it,
                    refBudget = b.id
            )
        }.toMutableList()

        repository.insert(*list.toTypedArray())

        return SampleResult(budgets, list, b)
    }

    @Test
    fun getByBudget() {

        runBlocking {
            val (budgets, list) = insertSample()
            repository.insert(
                    *(1..5).map {
                        SpareEntry(
                                comment = "Part $it",
                                amount = 100.0 * it,
                                refBudget = budgets[0].id
                        )
                    }.toMutableList()
                            .toTypedArray()
            )

            repository.insert(
                    *(1..5).map {
                        SpareEntry(
                                comment = "Part $it",
                                amount = 100.0 * it,
                                refBudget = null
                        )
                    }.toMutableList()
                            .toTypedArray()
            )


            budgets.forEach { b ->
                repository.getByBudget(b).forEach {
                    list.remove(it)
                }

                assertTrue(list.none { it.refBudget == b.id })
            }
        }
    }

    @Test
    fun getSum() {

        runBlocking {
            val (budgets, list, b) = insertSample()
            repository.insert(
                    *(1..5).map {
                        SpareEntry(
                                comment = "Part $it",
                                amount = 100.0 * it,
                                refBudget = budgets[0].id
                        )
                    }.toMutableList()
                            .toTypedArray()
            )

            repository.insert(
                    *(1..5).map {
                        SpareEntry(
                                comment = "Part $it",
                                amount = 100.0 * it,
                                refBudget = null
                        )
                    }.toMutableList()
                            .toTypedArray()
            )

            val sum1 = repository.getSum(b)
            val sum2 = list
                    .filter { it.refBudget == b.id }
                    .sumByDouble { it.amount }

            assertTrue(sum1 == sum2)
        }
    }

    @Test
    fun getBudgetPartWithAmount() {
        runBlocking {
            val (budgets, list, b) = insertSample(15)

            val parts = (1..5).map {
                BudgetPart(name = "Part $it")
                        .apply {
                            percent = it / 10.0
                            refBudget = b.id
                        }
            }.toMutableList()

            parts.last().closed = true
            parts.last().closeDate = LocalDateTime.now()
            assertTrue(parts.filter{!it.closed}.sumByDouble { it.percent } == 1.0)
            repositoryBudgetPart.insert(*parts.toTypedArray())

            val sum = repository.getSum(b)

            //testing the repartition SQL query
            // first case: the sum of percents is 100% -> repartition is 0
            assertTrue(repository.spareEntryDao.getRepartition(b.id!!) == 0.0)

            // second case: the sum of percents is 60% (budgets[3] is closed too)
            // -> repartition 40% reparteed to the not closed parts
            parts[parts.size - 2].apply {
                closed = true
                closeDate = LocalDateTime.now()
                repositoryBudgetPart.update(this)
            }
            val r = repository.spareEntryDao.getRepartition(b.id!!)
            assertTrue(parts.filter { !it.closed }.sumByDouble { it.percent + r } == 1.0)

            val total = repository.getBudgetPartWithAmount(b)
            assertTrue(total.sumByDouble { it.amount } == sum)
        }
    }

}