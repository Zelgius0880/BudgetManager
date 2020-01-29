package zelgius.com.budgetmanager.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_budget.view.*
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.setListeners

class BudgetDialog : DialogFragment() {
    var budget: Budget? = null
    var listener: ((Budget) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireActivity(), R.layout.dialog_budget, null)

        view.name.editText?.setText(budget?.name)


        return MaterialAlertDialogBuilder(ContextThemeWrapper(requireActivity(),R.style.ThemeOverlay_BudgetManager_MaterialAlertDialog))
                .setTitle(if (budget != null) R.string.new_budget else R.string.edit_budget)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create().apply {
                    setListeners(
                            positiveListener = {
                                if (view.name.editText?.text?.isEmpty() == false) {
                                    if(budget == null) budget = Budget()

                                    listener?.invoke((budget
                                            ?: Budget()).also { it.name = view.name.editText?.text?.toString()!! })
                                    true
                                } else {
                                    view.name.error = getString(R.string.name_required)
                                    false
                                }
                            }
                    )
                }
    }
}