package zelgius.com.budgetmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_budget.*
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.dialogs.BudgetDialog
import zelgius.com.budgetmanager.entities.Budget


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
            override fun areItemsTheSame(oldConcert: Budget,
                                         newConcert: Budget) = oldConcert.id == newConcert.id

            override fun areContentsTheSame(oldConcert: Budget,
                                            newConcert: Budget) = oldConcert == newConcert
        }
    }

    var itemClickListener: ((Budget) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_budget, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AdapterBudget().apply {
            itemClickListener = {
                BudgetDialog().apply {
                    budget = it
                    show(this@BudgetFragment.fragmentManager!!, "")
                }
            }
        }

        with(recyclerView) {
            this.adapter = adapter
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

                /*
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    val fab: FloatingActionButton = view.findViewById(R.id.add)
                    if(newState==RecyclerView.SCROLL_STATE_IDLE)
                        fab.show()
                }
                */
            })
        }

    }

    inner class AdapterBudget : PagedListAdapter<Budget, ViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_budget, parent, false))


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val budget = getItem(position)

            if(budget != null) {

                holder.itemView.setOnClickListener {

                    itemClickListener?.invoke(budget)
                }
            }
        }

    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}