package zelgius.com.budgetmanager.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_entry.view.*
import kotlinx.android.synthetic.main.fragment_pie.*
import zelgius.com.budgetmanager.*
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.entities.SpareEntry
import zelgius.com.budgetmanager.fragments.PieFragment
import zelgius.com.budgetmanager.viewModel.BudgetViewModel
import java.lang.NumberFormatException

class EntryDialog : DialogFragment() {
    var listener: ((SpareEntry) -> Unit)? = null
    val ctx by lazy {requireActivity()}
    var selectedBudget: Budget? = null

    private val viewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(BudgetViewModel::class.java)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireActivity(), R.layout.dialog_entry, null)

        viewModel.get(false).observe(this) {
            view.spinner.adapter = ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, it.map { b ->  b.name }.toTypedArray())

            if (selectedBudget != null) {
                val index = it.indexOfFirst {b -> selectedBudget?.id == b.id}

                if(index > 0) {
                    selectedBudget = it[index]
                    view.spinner.setSelection(index)
                }
            }

            view.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedBudget = it[position]
                }

            }
        }


        return MaterialAlertDialogBuilder(ContextThemeWrapper(requireActivity(),R.style.ThemeOverlay_BudgetManager_MaterialAlertDialog))
                .setTitle(R.string.entry_entry)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create().apply {
                    setListeners(
                            positiveListener = {
                                when (val number = format(view.amount.editText?.text?.toString()?:"")) {
                                    null -> {
                                        view.amount.error = getString(R.string.invalid_number)
                                        false
                                    }
                                    else -> {
                                        listener?.invoke(SpareEntry(
                                                comment = view.comment.editText?.text?.toString()?: "",
                                                amount = number,
                                                refBudget = selectedBudget?.id
                                        ))
                                        true
                                    }
                                }
                            }
                    )
                }
    }

    private fun format(s: String) =
        try {
            s.replace(',','.').toDouble()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            null
        }

}