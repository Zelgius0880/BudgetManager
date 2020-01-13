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
import zelgius.com.budgetmanager.entities.SpareEntry

class SpareEntryRepositoryTest {

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = SpareEntryRepositoryTestImpl(context)
        repositoryBudget = BudgetRepositoryTest.Companion.BudgetRepositoryTestImpl(context)
    }

    companion object {

        class SpareEntryRepositoryTestImpl(val context: Context) : SpareEntryRepository(context) {
            override val spareEntryDao by lazy { AppDatabase.getInstance(context, true).spareEntryDao }

            fun close() = AppDatabase.getInstance(context, true).close()
        }

        private lateinit var repository: SpareEntryRepositoryTestImpl
        private lateinit var repositoryBudget:  BudgetRepositoryTest.Companion.BudgetRepositoryTestImpl

        @JvmStatic
        @AfterClass
        fun closeDb(){
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

    @Test
    fun getBudgetAndPart() {
        val b = Budget(name = "Budget Test")

        runBlocking {
            repositoryBudget.insert(b)
            assertTrue(repositoryBudget.get().contains(b))

            val list = (1..5).map {
                SpareEntry(comment = "Netry $it")
                        .apply {
                            refBudget = b.id
                        }
            }.toMutableList()

            repository.insert(*list.toTypedArray())
            repository.getBudgetAndPart().forEach {
                println(it)
                it.budget == b
                list.remove(it.entry)
            }

            assertTrue(list.isEmpty())
        }
    }
}