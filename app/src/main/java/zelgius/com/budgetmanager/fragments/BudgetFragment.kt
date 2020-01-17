package zelgius.com.budgetmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_budget.view.*
import kotlinx.android.synthetic.main.fragment_budget.*
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.dao.BudgetAndPart
import zelgius.com.budgetmanager.dialogs.BudgetDialog
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.format
import zelgius.com.budgetmanager.observe
import zelgius.com.budgetmanager.view.SwipeToDeleteCallback
import zelgius.com.budgetmanager.viewModel.BudgetViewModel
import java.time.format.DateTimeFormatter


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */


class BudgetFragment : Fragment() {
    companion object {
        val TAG = BudgetDialog::class.java.simpleName

        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BudgetAndPart>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(old: BudgetAndPart,
                                         new: BudgetAndPart) = old.budget.id == new.budget.id && old.part?.id == new.part?.id

            override fun areContentsTheSame(old: BudgetAndPart,
                                            new: BudgetAndPart) = old == new
        }
    }

    val adapter by lazy {
        AdapterBudget().apply {
            budgetClickListener = {
                showBudgetDialog(it)
            }

            partClickListener = {
            }
        }
    }

    val viewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(BudgetViewModel::class.java)
    }

    var budgetClickListener: ((Budget) -> Unit)? = null
    var partClickListener: ((BudgetPart) -> Unit)? = null
    lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_budget, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView = view

        with(recyclerView) {
            adapter = this@BudgetFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val fab: FloatingActionButton = view.findViewById(R.id.add)
                    if (dy < 0 && fab.visibility == View.VISIBLE) {
                        fab.hide()
                    } else if (dy > 0 && fab.visibility != View.VISIBLE) {
                        fab.show()
                    }
                }
            })
            enableSwipeToDeleteAndUndo()
        }

        add.setOnClickListener {
            showBudgetDialog()
        }

        viewModel.getPagedList().observe(this) {
            adapter.submitList(it)
        }
    }

    private fun showBudgetDialog(budget: Budget? = null) {
        BudgetDialog().apply {
            this.budget = budget
            listener = { b ->
                viewModel.save(b).observe(this@BudgetFragment) {
                    if (it)
                        Snackbar.make(mView, R.string.save_ok, Snackbar.LENGTH_SHORT).show()
                }
            }
        }.show(fragmentManager!!, "dialog_budget")
    }

    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.getData(position)

                if (item != null) {
                    viewModel.delete(item.budget).observe(this@BudgetFragment) {
                        Snackbar.make(mView, R.string.snack_delete_success, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo) {
                                    item.budget.id = null
                                    viewModel.save(item.budget)
                                    recyclerView.scrollToPosition(position)
                                }
                                //.setActionTextColor(Color.YELLOW)
                                .show()
                    }
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    inner class AdapterBudget : PagedListAdapter<BudgetAndPart, BudgetViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder =
                BudgetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_budget, parent, false))


        override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
            val item = getItem(position)

            if (item != null) {
                if(position > 0 && getItem(position -1)?.budget == item.budget)
                    holder.itemView.budget.visibility = View.GONE
                else {
                    holder.itemView.budgetName.text = item.budget.name
                    holder.itemView.budgetStartDate.text = item.budget.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                }

                if(item.part == null)
                    holder.itemView.part.visibility = View.GONE
                else {
                    holder.itemView.budgetName.text = item.part.name
                    holder.itemView.budgetStartDate.text = item.part.goal.format(2)
                }


                holder.itemView.budget.setOnClickListener {
                    budgetClickListener?.invoke(item.budget)
                }

                holder.itemView.part.setOnClickListener {
                    partClickListener?.invoke(item.part!!)
                }

                holder.itemView.rippleBackground.setOnLongClickListener {
                    Snackbar.make(mView, "Looooong", Snackbar.LENGTH_SHORT).show()

                    true
                }
            }
        }

        fun getData(position: Int) = getItem(position)

    }

    class BudgetViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}