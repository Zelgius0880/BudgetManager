package zelgius.com.budgetmanager.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedListAdapter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.view.ImplementsAlphaChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_part_home.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.name
import kotlinx.android.synthetic.main.fragment_home.recyclerView
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import zelgius.com.budgetmanager.ColorGenerator
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.dao.BudgetAndEntry
import zelgius.com.budgetmanager.dao.BudgetAndPart
import zelgius.com.budgetmanager.dao.BudgetPartWithAmount
import zelgius.com.budgetmanager.dialogs.EntryDialog
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.observe
import zelgius.com.budgetmanager.observeOnce
import zelgius.com.budgetmanager.viewModel.BudgetViewModel
import zelgius.com.budgetmanager.viewModel.EntryViewModel
import java.text.DecimalFormat


/** A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : ChartFragment() {

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BudgetPartWithAmount>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(old: BudgetPartWithAmount,
                                         new: BudgetPartWithAmount) = old == new

            override fun areContentsTheSame(old: BudgetPartWithAmount,
                                            new: BudgetPartWithAmount) =
                    old.part.id == new.part.id && old.amount == new.amount
        }
    }

    private val budgetViewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(BudgetViewModel::class.java)
    }

    private val entryViewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(EntryViewModel::class.java)
    }

    private var budget: Budget? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private lateinit var mView: View
    private var parts = mutableListOf<BudgetPart>()
    private var adapter: Adapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mView = view
        view.findViewById<BottomAppBar>(R.id.bottomBar)
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_edit -> {
                            findNavController().navigate(R.id.action_homeFragment_to_budgetFragment)
                            true
                        }

                        R.id.menu_donut -> {
                            findNavController().navigate(R.id.action_homeFragment_to_pieFragment)
                            true
                        }

                        R.id.menu_spare_entries -> {
                            findNavController().navigate(R.id.action_homeFragment_to_entryFragment)
                            true
                        }
                        else -> false
                    }
                }

        recyclerView.adapter = adapter
        refreshGraph()

        fab.setOnClickListener {
            EntryDialog().apply {
                selectedBudget = budget
                listener = {
                    entryViewModel.save(it).observeOnce(this) {
                        Snackbar.make(view.coordinator, R.string.save_ok, Snackbar.LENGTH_SHORT)
                                .setAnchorView(view.fab)
                                .show()
                    }
                }
            }
                    .show(parentFragmentManager, null)
        }
    }

    private fun refreshGraph() {
        val id = PreferenceManager.getDefaultSharedPreferences(requireContext()).getLong("BUDGET_ID", -1L)

        if (id > 0) {
            budgetViewModel.getPartAndAmount(id, greaterThanZero = true).observe(this) {
                parts = it.map { b -> b.part }.toMutableList()
            }

            budgetViewModel.get(id).observeOnce(this) {
                if (it != null) {
                    if (adapter == null) {
                        adapter = Adapter()
                        recyclerView.adapter = adapter
                        budgetViewModel.getPartAndAmountPagedList(it).observe(this@HomeFragment) { list ->
                            adapter?.submitList(list)
                        }

                    }

                    name.text = it.name
                    budgetViewModel.getPart(it, true).observeOnce(this) { parts ->
                        setUpChart(chart, parts, recyclerView)
                    }
                }
            }
        }
    }


    override fun setUpChart(chart: PieChart, list: List<BudgetPart>, recyclerView: RecyclerView?) {
        super.setUpChart(chart, list, recyclerView)
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {}

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val item = e?.data
                if (item is BudgetPart) {
                    val index = parts.indexOfFirst { it.id == item.id }
                    if (recyclerView != null && index >= 0)
                        (recyclerView.layoutManager as? LinearLayoutManager)?.smoothScrollToPosition(recyclerView, RecyclerView.State(), index)
                }
            }

        })
    }

    inner class Adapter : PagedListAdapter<BudgetPartWithAmount, ViewHolder>(DIFF_CALLBACK) {
        private val colorGenerator = ColorGenerator(requireContext())
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_part_home, parent, false))

        @ImplementsAlphaChart
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position) ?: return

            holder.itemView.entryName.text = item.part.name
            holder.itemView.imageView.visibility = View.GONE
            holder.itemView.materialCardView.isChecked = item.part.closed
            holder.itemView.materialCardView.setOnLongClickListener {
                holder.itemView.materialCardView.isChecked = !holder.itemView.materialCardView.isChecked
                budgetViewModel.close(item.part, !item.part.closed).observeOnce(this@HomeFragment) {
                    Snackbar.make(mView.coordinator, R.string.save_ok, Snackbar.LENGTH_SHORT)
                            .setAnchorView(mView.fab)
                            .show()

                    refreshGraph()
                }
                true

            }
            holder.itemView.progress.apply {
                val color = colorGenerator.getColor(item.part.name + item.part.id)
                donutColors = intArrayOf(color)
                animation.duration = 1000L
                animate(listOf((item.amount / item.part.goal).toFloat() * 100f))

                holder.itemView.motion.setTransitionDuration(1)
                holder.itemView.motion.transitionToStart()

                Handler().apply {
                    var current = 0.0
                    val i = item.amount / (1000.0 / 50.0) // a view is refreshed +- every 16ms in the best conditions. Dividing by 50 to let the time to the view to be refreshed

                    var work: (() -> Unit)? = null
                    work = {
                        val format = DecimalFormat("0.#")
                        holder.itemView.progressText.text = String.format("%s/%s €",
                                format.format(current), format.format(item.part.goal))

                        current += i
                        if (current < item.amount) postDelayed(50L, work!!)
                        else {
                            holder.itemView.progressText.text = String.format("%s/%s €",
                                    format.format(item.amount), format.format(item.part.goal))

                            if (item.amount >= item.part.goal) {
                                if (!item.part.reached) {
                                    holder.itemView.motion.setTransitionDuration(1000)
                                    budgetViewModel.save(item.part.apply { item.part.reached = true })
                                    holder.itemView.motion.transitionToEnd()
                                } else {
                                    holder.itemView.motion.setTransitionDuration(1)
                                    holder.itemView.motion.transitionToEnd()
                                }
                            }
                        }

                    }
                    postDelayed(50L, work)
                }
            }

        }

    }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}