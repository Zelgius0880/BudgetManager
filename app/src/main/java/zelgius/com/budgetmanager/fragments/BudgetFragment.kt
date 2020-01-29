package zelgius.com.budgetmanager.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_budget.view.*
import kotlinx.android.synthetic.main.fragment_budget.*
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.dao.BudgetAndPart
import zelgius.com.budgetmanager.dialogs.BudgetDialog
import zelgius.com.budgetmanager.dialogs.PartDialog
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.format
import zelgius.com.budgetmanager.observe
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
                                            new: BudgetAndPart) = old.budget == new.budget && old.part == new.part
        }
    }

    private val adapter by lazy {
        AdapterBudget().apply {
            budgetClickListener = {
                showBudgetDialog(it)
            }

            partClickListener = { b, p ->
                showPartDialog(b, p)
            }

            partDeleteListener = {
                viewModel.delete(it).observe(this@BudgetFragment) { _ ->
                    showUndoSnackbar(it)
                }
            }

            budgetDeleteListener = {
                viewModel.delete(it).observe(this@BudgetFragment) { _ ->
                    showUndoSnackbar(it)
                }
            }

            budgetCloseListener = {
                viewModel.closeBudget(!it.closed, it)
            }

            partCloseListener = {
                it.closed = !it.closed
                viewModel.save(it)
            }
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(BudgetViewModel::class.java)
    }

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
                        Snackbar.make(mView, R.string.save_ok, Snackbar.LENGTH_SHORT).show()
                }
            }
        }.show(parentFragmentManager, "dialog_budget")
    }

    private fun showPartDialog(budget: Budget, part: BudgetPart? = null) {
        PartDialog().apply {
            this.part = if (part == null) null else BudgetPart(part.id, part.percent, part.name, part.goal, part.closed, part.reached, part.refBudget)
            listener = { p ->
                viewModel.save(budget, p).observe(this@BudgetFragment) {
                    if (it)
                        Snackbar.make(mView, R.string.save_ok, Snackbar.LENGTH_SHORT).show()
                }
            }
        }.show(parentFragmentManager, "dialog_part")
    }

    private fun showUndoSnackbar(item: Budget) {
        Snackbar.make(mView, R.string.snack_delete_success, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    item.id = null
                    viewModel.save(item)
                    //recyclerView.scrollToPosition(position)
                }
                .show()
    }

    private fun showUndoSnackbar(item: BudgetPart) {
        Snackbar.make(mView, R.string.snack_delete_success, Snackbar.LENGTH_LONG)
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

    class AdapterBudget : PagedListAdapter<BudgetAndPart, BudgetViewHolder>(DIFF_CALLBACK) {
        var budgetClickListener: ((Budget) -> Unit)? = null
        var partClickListener: ((Budget, BudgetPart?) -> Unit)? = null

        var budgetDeleteListener: ((Budget) -> Unit)? = null
        var partDeleteListener: ((BudgetPart) -> Unit)? = null

        var partCloseListener: ((BudgetPart) -> Unit)? = null
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

                if (position > 0 && getItem(position - 1)?.budget == item.budget) {
                    holder.itemView.budget.visibility = View.GONE

                } else {
                    holder.itemView.budget.visibility = View.VISIBLE
                    holder.itemView.name.text = item.budget.name
                    holder.itemView.budgetStartDate.text = item.budget.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                    if (item.budget.closed) {
                        holder.itemView.closeBudget.setIconResource(R.drawable.ic_baseline_check_circle_24)
                        holder.itemView.closeBudget.setIconTintResource(R.color.md_blue_700)
                    } else {
                        holder.itemView.closeBudget.setIconResource(R.drawable.ic_baseline_check_circle_outline_24)
                        holder.itemView.closeBudget.setIconTintResource(R.color.md_black_1000)
                    }

                    holder.itemView.closeBudget.setOnClickListener {
                        budgetClickListener?.invoke(item.budget)
                    }

                    holder.itemView.budget.setOnClickListener {
                        budgetClickListener?.invoke(item.budget)
                    }

                    holder.itemView.budgetAdd.setOnClickListener {
                        partClickListener?.invoke(item.budget, null)
                    }

                    holder.itemView.deleteBudget.setOnClickListener {
                        budgetDeleteListener?.invoke(item.budget)
                    }


                    holder.itemView.closeBudget.setOnClickListener {
                        val drawable = AnimatedVectorDrawableCompat
                                .create(holder.itemView.context,
                                        if (item.budget.closed)
                                            R.drawable.avd_checked_to_unchecked
                                        else
                                            R.drawable.avd_unchecked_to_checked
                                )?.apply {
                                    if (!item.budget.closed) {
                                        holder.itemView.closeBudget.setIconTintResource(R.color.md_blue_700)
                                    } else {
                                        holder.itemView.closeBudget.setIconTintResource(R.color.md_black_1000)
                                    }

                                    registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                                        override fun onAnimationEnd(drawable: Drawable?) {
                                            super.onAnimationEnd(drawable)
                                            budgetCloseListener?.invoke(item.budget)
                                            unregisterAnimationCallback(this)
                                        }
                                    })
                                }


                        holder.itemView.closeBudget.icon = drawable
                        drawable?.start()
                    }
                }

                holder.itemView.deleteBudget.visibility = if (item.part == null) View.VISIBLE else View.GONE

                if (item.part == null)
                    holder.itemView.part.visibility = View.GONE
                else {
                    holder.itemView.part.visibility = View.VISIBLE
                    holder.itemView.partName.text = item.part.name
                    holder.itemView.partGoal.text = String.format("%s€", item.part.goal.format(2))

                    if (item.part.closed) {
                        holder.itemView.closePart.setIconResource(R.drawable.ic_baseline_check_circle_24)
                        holder.itemView.closePart.setIconTintResource(R.color.md_blue_700)
                    } else {
                        holder.itemView.closePart.setIconResource(R.drawable.ic_baseline_check_circle_outline_24)
                        holder.itemView.closePart.setIconTintResource(R.color.md_black_1000)
                    }

                    holder.itemView.part.setOnClickListener {
                        partClickListener?.invoke(item.budget, item.part)
                    }

                    holder.itemView.deletePart.setOnClickListener {
                        partDeleteListener?.invoke(item.part)
                    }

                    holder.itemView.closePart.setOnClickListener {
                        val drawable = AnimatedVectorDrawableCompat
                                .create(holder.itemView.context,
                                        if (item.part.closed)
                                            R.drawable.avd_checked_to_unchecked
                                        else
                                            R.drawable.avd_unchecked_to_checked
                                )?.apply {
                                    if (!item.part.closed) {
                                        holder.itemView.closePart.setIconTintResource(R.color.md_blue_700)
                                    } else {
                                        holder.itemView.closePart.setIconTintResource(R.color.md_black_1000)
                                    }

                                    registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                                        override fun onAnimationEnd(drawable: Drawable?) {
                                            super.onAnimationEnd(drawable)
                                            partCloseListener?.invoke(item.part)
                                            unregisterAnimationCallback(this)
                                        }
                                    })
                                }


                        holder.itemView.closePart.icon = drawable
                        drawable?.start()
                    }

                    /*holder.itemView.rippleBackgroundBudget.setOnLongClickListener {
                        partLongPressListener?.invoke(item.part)
                        true
                    }*/
                }

            }
        }

        fun getData(position: Int) = getItem(position)

    }

    class BudgetViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}