package zelgius.com.budgetmanager.repositories

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.AfterClass

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import zelgius.com.budgetmanager.entities.Budget
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class BudgetRepositoryTest {

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = BudgetRepositoryTestImpl(context)
    }
    
    companion object {
        class BudgetRepositoryTestImpl(val context: Context) : BudgetRepository(context) {
            override val budgetDao  by lazy { AppDatabase.getInstance(context, true).budgetDao }

            fun close() = AppDatabase.getInstance(context, true).close()
        }
        private lateinit var repository: BudgetRepositoryTestImpl

        @JvmStatic
        @AfterClass
        fun closeDb(){
            repository.close()
        }
    }


    @Test
    fun insert() {
        val b = Budget(name = "Budget Test")

        runBlocking {
            repository.insert(b)
            val list = repository.get()

            assertTrue(list.isNotEmpty())
            assertTrue(list.contains(b))
        }

    }

    @Test
    fun update() {
        val b = Budget(name = "Budget Test")

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
        val b = Budget(name = "Budget Test")

        runBlocking {
            repository.insert(b)
            assertTrue(repository.get().contains(b))

            repository.delete(b)
            assertFalse(repository.get().contains(b))
        }
    }

    @Test
    fun get() {

        runBlocking {
            val list  = mutableListOf<Budget>()
            for(i in 1..5){
                with(Budget(name = "Budget Test $i")) {
                    repository.insert(this)
                    list.add(this)
                }
            }
            assertTrue(repository.get().containsAll(list))
        }
    }
}