package zelgius.com.budgetmanager.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.adapter_budget.*
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.entities.Budget

class BudgetDialog : DialogFragment() {
    var budget: Budget? = null
    var listener: ((Budget) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_BudgetManager_MaterialAlertDialog)
                .setTitle(if (budget != null) R.string.new_budget else R.string.edit_budget)
                .setView(R.layout.dialog_budget)
                .setPositiveButton(android.R.string.ok, null)
                .create().apply {
                    if (name.editableText.isNullOrEmpty())
                        listener?.invoke((budget
                                ?: Budget()).also { it.name = name.editableText.toString() })
                }
    }
}