package zelgius.com.budgetmanager.repositories

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.AfterClass
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart

class BudgetPartRepositoryTest {

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = BudgetPartRepositoryTestImpl(context)
        repositoryBudget =  BudgetRepositoryTest.Companion.BudgetRepositoryTestImpl(context)
    }

    companion object {
        class BudgetPartRepositoryTestImpl(val context: Context) : BudgetPartRepository(context) {
            override val budgetPartDao by lazy { AppDatabase.getInstance(context, true).budgetPartDao }

            fun close() = AppDatabase.getInstance(context, true).close()
        }

        private lateinit var repository: BudgetPartRepositoryTestImpl
        private lateinit var repositoryBudget:  BudgetRepositoryTest.Companion.BudgetRepositoryTestImpl

        @JvmStatic
        @AfterClass
        fun closeDb(){
            repository.close()
        }

    }

    @Test
    fun insert() {
        val b = BudgetPart(name = "Budget Test")

        runBlocking {
            repository.insert(b)
            val list = repository.get()

            assertTrue(list.isNotEmpty())
            assertTrue(list.contains(b))
        }

    }

    @Test
    fun update() {
        val b = BudgetPart(name = "Budget Test")

        runBlocking {
            repository.insert(b)
            assertTrue(repository.get().contains(b))

            b.name += " test"
            repository.update(b)
            assertTrue(repository.get().contains(b))
        }

    }

    @Test
    fun delete() {
        val b = BudgetPart(name = "Budget Test")

        runBlocking {
            repository.insert(b)
            assertTrue(repository.get().contains(b))

            repository.delete(b)
            assertFalse(repository.get().contains(b))
        }
    }

    @Test
    fun getBudgetAndPart() {
        val b = Budget(name = "Budget Test")

        runBlocking {
            repositoryBudget.insert(b)
            assertTrue(repositoryBudget.get().contains(b))

            val list = (1..5).map {
                BudgetPart(name = "Part $it")
                        .apply {
                            refBudget = b.id
                        }
            }.toMutableList()

            repository.insert(*list.toTypedArray())
            repository.getBudgetAndPart().forEach {
                it.budget == b
                list.remove(it.part)
            }

            assertTrue(list.isEmpty())
        }
    }
}