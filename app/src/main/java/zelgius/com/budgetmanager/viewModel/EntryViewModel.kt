package zelgius.com.budgetmanager.viewModel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.toLiveData
import kotlinx.coroutines.launch
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.entities.SpareEntry
import zelgius.com.budgetmanager.repositories.BudgetPartRepository
import zelgius.com.budgetmanager.repositories.BudgetRepository
import zelgius.com.budgetmanager.repositories.SpareEntryRepository

class EntryViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = SpareEntryRepository(app)

    fun getBudgetAndEntryDataSource() = repository.getBudgetAndEntryDataSource().toLiveData(pageSize = 50)

    fun save(entry: SpareEntry): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {

            if (entry.id == null)
                repository.insert(entry)
            else
                repository.update(entry)

            result.postValue(true)
        }

        return result
    }


    fun delete(entry: SpareEntry): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.delete(entry)
            result.postValue(true)
        }

        return result
    }

    fun get(budget: Budget): LiveData<List<SpareEntry>> {
        val result = MutableLiveData<List<SpareEntry>>()
        viewModelScope.launch {
            result.postValue(repository.getByBudget(budget))
        }

        return result
    }


    fun getSum(budget: Budget): LiveData<Double> {
        val result = MutableLiveData<Double>()
        viewModelScope.launch {
            result.postValue(repository.getSum(budget))
        }

        return result
    }

}