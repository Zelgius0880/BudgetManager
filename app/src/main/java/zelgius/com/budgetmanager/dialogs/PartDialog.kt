package zelgius.com.budgetmanager.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_part.view.*
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.format
import zelgius.com.budgetmanager.setListeners

class PartDialog : DialogFragment() {
    var part = BudgetPart()
    var listener: ((BudgetPart, Budget?) -> Unit)? = null
    var budgets = listOf<Budget>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireActivity(), R.layout.dialog_part, null)

        if(part.id != null) {
            view.name?.editText?.setText(part.name)
            view.goal?.editText?.setText(part.goal.format(2))
        }

        val names =  budgets.map { b -> b.name }
                .toMutableList().apply {
                    add(0, requireContext().getString(R.string.not_associated))
                }

        view.spinner.adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, names.toTypedArray())
        val index = budgets.indexOfFirst { it.id == part.refBudget }
        view.spinner.setSelection(index + 1)

        return MaterialAlertDialogBuilder(ContextThemeWrapper(requireActivity(), R.style.ThemeOverlay_BudgetManager_MaterialAlertDialog))
                .setTitle(if (part.id == null) R.string.new_budget else R.string.edit_budget)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create().apply {
                    setListeners(
                            positiveListener = {
                                val number = format(view.goal?.editText?.text?.toString() ?: "")
                                when {
                                    view.name?.editText?.text.isNullOrEmpty() -> {
                                        view.name.error = getString(R.string.name_required)
                                        false
                                    }

                                    number == null -> {
                                        view.goal.error = getString(R.string.invalid_number)
                                        false
                                    }

                                    else -> {
                                        listener?.invoke(part.also {
                                            it.name = view.name?.editText?.text?.toString()!!
                                            it.goal = number
                                        },
                                                if (view.spinner.selectedItemPosition == 0) null
                                                else budgets[view.spinner.selectedItemPosition - 1])
                                        true
                                    }
                                }
                            }
                    )
                }
    }

    fun format(s: String) =
            try {
                s.replace(',', '.').toDouble()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                null
            }

}