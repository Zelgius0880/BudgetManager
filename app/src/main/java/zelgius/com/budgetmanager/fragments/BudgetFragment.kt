package zelgius.com.budgetmanager.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_budget.view.*
import kotlinx.android.synthetic.main.fragment_budget.*
import kotlinx.android.synthetic.main.fragment_budget.recyclerView
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.dialogs.BudgetDialog
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.observe
import zelgius.com.budgetmanager.viewModel.BudgetViewModel
import zelgius.com.swipetodelete.SwipeToDeletePagedAdapter
import java.time.format.DateTimeFormatter


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */


class BudgetFragment : Fragment() {
    companion object {
        val TAG = BudgetDialog::class.java.simpleName

        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<Budget>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(old: Budget,
                                         new: Budget) = old.id == new.id

            override fun areContentsTheSame(old: Budget,
                                            new: Budget) = old == new
        }
    }

    private val adapter by lazy {
        AdapterBudget {
            viewModel.delete(it).observe(this@BudgetFragment) { _ ->
                showUndoSnackbar(it)
            }
        }.apply {
            budgetClickListener = {
                showBudgetDialog(it)
            }

            budgetCloseListener = {
                viewModel.closeBudget(!it.closed, it)
            }
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(BudgetViewModel::class.java)
    }
    private val navController by lazy { findNavController() }

    private lateinit var _view: View
    private val activity by lazy { requireActivity() as AppCompatActivity }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_budget, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         _view = view

        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            toolbarTitle.setText(zelgius.com.budgetmanager.R.string.edit_budget)
        }

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
            //enableSwipeToDeleteAndUndo()
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
                        Snackbar.make(_view, R.string.save_ok, Snackbar.LENGTH_SHORT).show()
                }
            }
        }.show(parentFragmentManager, "dialog_budget")
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> navController.navigateUp()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showUndoSnackbar(item: Budget) {
        Snackbar.make(_view, R.string.snack_delete_success, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    item.id = null
                    viewModel.save(item)
                    //recyclerView.scrollToPosition(position)
                }
                .show()
    }

/*
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //adapter.saveStates(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //adapter.restoreStates(savedInstanceState)
    }
*/

    class AdapterBudget(deleteListener: (Budget) -> Unit) : SwipeToDeletePagedAdapter<Budget, BudgetViewHolder>(DIFF_CALLBACK, deleteListener = deleteListener) {
        var budgetClickListener: ((Budget) -> Unit)? = null
        var budgetCloseListener: ((Budget) -> Unit)? = null

        /*private val viewBinderHelper = ViewBinderHelper().apply {
            setOpenOnlyOne(true)
        }

        fun saveStates(outState: Bundle?) {
            viewBinderHelper.saveStates(outState)
        }

        fun restoreStates(inState: Bundle?) {
            viewBinderHelper.restoreStates(inState)
        }*/

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder =
                BudgetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_budget, parent, false))


        override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
            val item = getItem(position)

            if (item != null) {
                /*viewBinderHelper.bind(holder.itemView.budgetSwipe, "${item.budget.id!! + (item.part?.id?:0L)}")
                viewBinderHelper.bind(holder.itemView.partSwipe, "${item.budget.id!! + (item.part?.id?:0L)}")*/

                holder.itemView.budget.visibility = View.VISIBLE
                holder.itemView.name.text = item.name
                holder.itemView.budgetStartDate.text = item.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                if (item.closed) {
                    holder.itemView.closeBudget.setIconResource(R.drawable.ic_baseline_check_circle_24)
                    holder.itemView.closeBudget.setIconTintResource(R.color.md_blue_700)
                } else {
                    holder.itemView.closeBudget.setIconResource(R.drawable.ic_baseline_check_circle_outline_24)
                    holder.itemView.closeBudget.setIconTintResource(R.color.md_black_1000)
                }

                holder.itemView.closeBudget.setOnClickListener {
                    budgetClickListener?.invoke(item)
                }

                holder.itemView.budget.setOnClickListener {
                    budgetClickListener?.invoke(item)
                }

                holder.itemView.closeBudget.setOnClickListener {
                    val drawable = AnimatedVectorDrawableCompat
                            .create(holder.itemView.context,
                                    if (item.closed)
                                        R.drawable.avd_checked_to_unchecked
                                    else
                                        R.drawable.avd_unchecked_to_checked
                            )?.apply {
                                if (!item.closed) {
                                    holder.itemView.closeBudget.setIconTintResource(R.color.md_blue_700)
                                } else {
                                    holder.itemView.closeBudget.setIconTintResource(R.color.md_black_1000)
                                }

                                registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                                    override fun onAnimationEnd(drawable: Drawable?) {
                                        super.onAnimationEnd(drawable)
                                        budgetCloseListener?.invoke(item)
                                        unregisterAnimationCallback(this)
                                    }
                                })
                            }


                    holder.itemView.closeBudget.icon = drawable
                    drawable?.start()
                }
            }

        }
    }

    class BudgetViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}