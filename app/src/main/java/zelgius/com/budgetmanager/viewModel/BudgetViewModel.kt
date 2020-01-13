package zelgius.com.budgetmanager.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.toLiveData
import zelgius.com.budgetmanager.repositories.BudgetRepository

class BudgetViewModel(app: Application): AndroidViewModel(app) {
    val repository = BudgetRepository(app)

    fun getPagedList() = repository.getDataSource().toLiveData(pageSize = 50)
}