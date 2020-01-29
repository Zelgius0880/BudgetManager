package zelgius.com.budgetmanager.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_budget.view.*
import kotlinx.android.synthetic.main.dialog_budget.view.name
import kotlinx.android.synthetic.main.dialog_part.view.*
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.format
import zelgius.com.budgetmanager.setListeners
import java.lang.NumberFormatException

class PartDialog : DialogFragment() {
    var part: BudgetPart? = null
    var listener: ((BudgetPart) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireActivity(), R.layout.dialog_part, null)

        view.name.editText?.setText(part?.name)
        view.goal.editText?.setText(part?.goal?.format(2))


        return MaterialAlertDialogBuilder(ContextThemeWrapper(requireActivity(),R.style.ThemeOverlay_BudgetManager_MaterialAlertDialog))
                .setTitle(if (part != null) R.string.new_budget else R.string.edit_budget)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create().apply {
                    setListeners(
                            positiveListener = {
                                val number = format(view.goal.editText?.text?.toString()?:"")
                                when{
                                    view.name.editText!!.text.isNullOrEmpty() -> {
                                        view.name.error = getString(R.string.name_required)
                                        false
                                    }

                                    number == null -> {
                                        view.goal.error = getString(R.string.invalid_number)
                                        false
                                    }

                                    else -> {
                                        if(part == null) part = BudgetPart()

                                        listener?.invoke((part
                                                ?: BudgetPart()).also {
                                            it.name = view.name.editText?.text?.toString()!!
                                            it.goal = number
                                        })
                                        true
                                    }
                                }
                            }
                    )
                }
    }

    fun format(s: String) =
        try {
            s.replace(',','.').toDouble()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            null
        }

}