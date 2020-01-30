package zelgius.com.budgetmanager.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.view.ImplementsAlphaChart
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_part_main.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.recyclerView
import kotlinx.android.synthetic.main.fragment_home.view.*
import zelgius.com.budgetmanager.ColorGenerator
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.dao.BudgetPartWithAmount
import zelgius.com.budgetmanager.dialogs.EntryDialog
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.observeOnce
import zelgius.com.budgetmanager.viewModel.BudgetViewModel
import zelgius.com.budgetmanager.viewModel.EntryViewModel
import java.text.DecimalFormat


/** A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : ChartFragment() {


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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

        val id = PreferenceManager.getDefaultSharedPreferences(requireContext()).getLong("BUDGET_ID", -1L)

        if (id > 0) {
            budgetViewModel.getPartAndAmount(id, greaterThanZero = true).observeOnce(this) {
                recyclerView.adapter = Adapter(it)
                setUpChart(chart, it.map {i -> i.part }, recyclerView)
            }

            budgetViewModel.get(id).observeOnce(this) {
                if (it != null)
                    name.text = it.name
            }
        }

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

    inner class Adapter(val list: List<BudgetPartWithAmount>) : RecyclerView.Adapter<ViewHolder>() {
        private val colorGenerator = ColorGenerator(requireContext())
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_part_main, parent, false))

        override fun getItemCount(): Int = list.size

        @ImplementsAlphaChart
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.itemView.entryName.text = item.part.name
            holder.itemView.imageView.visibility = View.GONE
            holder.itemView.progress.apply {
                val color = colorGenerator.getColor(item.part.name + item.part.id)
                donutColors = intArrayOf(color)
                animation.duration = 1000L
                animate(listOf((item.amount / item.part.goal).toFloat() *100f))

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

                            if(item.amount >= item.part.goal)
                                holder.itemView.motion.transitionToEnd()
                        }

                    }
                    postDelayed(50L, work)
                }
            }

        }

    }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
}