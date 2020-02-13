package zelgius.com.budgetmanager.viewModel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.paging.toLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zelgius.com.budgetmanager.dao.BudgetPartWithAmount
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.repositories.BudgetPartRepository
import zelgius.com.budgetmanager.repositories.BudgetRepository
import zelgius.com.budgetmanager.repositories.SpareEntryRepository
import java.time.LocalDateTime

class BudgetViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = BudgetRepository(app)
    private val repositoryPart = BudgetPartRepository(app)
    private val repositoryEntry = SpareEntryRepository(app)

    fun getPagedList() = repository.getDataSource().toLiveData(pageSize = 50)
    fun getPartPagedList(budget: Budget): LiveData<PagedList<BudgetPart>> = repositoryPart.getDataSource(budget).toLiveData(pageSize = 50)
    fun getPartAndAmountPagedList(budget: Budget) =
                        repositoryEntry.getPartAndAmountDataSource(budget)
                                .toLiveData(pageSize = 50)

    fun save(budget: Budget): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {

            if (budget.id == null)
                repository.insert(budget)
            else
                repository.update(budget)

            result.postValue(true)
        }

        return result
    }

    fun save(budget: Budget?, part: BudgetPart): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            part.refBudget = budget?.id

            if (part.id == null)
                repositoryPart.insert(part)
            else
                repositoryPart.update(part)

            result.postValue(true)
        }

        return result
    }

    fun save(part: BudgetPart): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {

            if (part.id == null)
                repositoryPart.insert(part)
            else
                repositoryPart.update(part)

            result.postValue(true)
        }

        return result
    }

    fun close(part: BudgetPart, closed: Boolean = true): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {

            part.closed = closed
            part.closeDate = LocalDateTime.now()
            repositoryPart.update(part)

            result.postValue(true)
        }

        return result
    }

    fun closeBudget(closed: Boolean, budget: Budget): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.update(budget.apply { this.closed = closed })
            repository.closeBudget(closed, budget)
            result.postValue(true)
        }

        return result
    }

    fun delete(budget: Budget): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.delete(budget)
            result.postValue(true)
        }

        return result
    }

    fun delete(budgetPart: BudgetPart): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repositoryPart.delete(budgetPart)
            result.postValue(true)
        }

        return result
    }

    fun get(closed: Boolean): LiveData<List<Budget>> {
        val result = MutableLiveData<List<Budget>>()
        viewModelScope.launch {
            result.postValue(repository.get(closed))
        }

        return result
    }


    fun get(id: Long): LiveData<Budget?> {
        val result = MutableLiveData<Budget?>()
        viewModelScope.launch {
            result.postValue(repository.get(id))
        }

        return result
    }

    fun getPart(budget: Budget, ignoreClosed: Boolean = false): LiveData<List<BudgetPart>> {
        val result = MutableLiveData<List<BudgetPart>>()
        viewModelScope.launch {
            result.postValue(repositoryPart.get(budget.id!!, ignoreClosed))
        }

        return result
    }

    fun getPartAndAmount(budgetId: Long, greaterThanZero: Boolean = false): LiveData<List<BudgetPartWithAmount>> {
        val result = MutableLiveData<List<BudgetPartWithAmount>>()
        viewModelScope.launch {
            val b = repository.get(budgetId)

            if (b != null) {
                result.postValue(
                        repositoryEntry.getBudgetPartWithAmount(b).apply {
                            if (greaterThanZero)
                                filter { it.part.percent > 0.0 }
                        }/*.sortedBy {
                            when {
                                it.part.closed -> Double.MAX_VALUE
                                it.amount / it.part.goal >= 1 ->
                                    if (it.part.reached) Double.MAX_VALUE - 1 else -1.0
                                else -> it.amount / it.part.goal
                            }
                        }*/
                )
            }
        }

        return result
    }
}