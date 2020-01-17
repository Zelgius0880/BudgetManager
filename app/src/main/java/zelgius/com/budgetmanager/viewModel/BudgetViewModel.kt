package zelgius.com.budgetmanager.viewModel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.toLiveData
import kotlinx.coroutines.launch
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.repositories.BudgetPartRepository
import zelgius.com.budgetmanager.repositories.BudgetRepository

class BudgetViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = BudgetRepository(app)
    private val repositoryPart = BudgetPartRepository(app)

    fun getPagedList() = repositoryPart.getDataSource().toLiveData(pageSize = 50)

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

    fun delete(budget: Budget): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.delete(budget)
            result.postValue(true)
        }

        return result
    }
}