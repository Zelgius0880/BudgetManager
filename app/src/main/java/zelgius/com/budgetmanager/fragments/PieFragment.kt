package zelgius.com.budgetmanager.fragments

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_part_pie.view.*
import kotlinx.android.synthetic.main.fragment_pie.*
import zelgius.com.budgetmanager.*
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.viewModel.BudgetViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [PieFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PieFragment : ChartFragment() {

    private val navController by lazy { findNavController() }
    private val activity by lazy { requireActivity() as AppCompatActivity }
    lateinit var budgets: List<Budget>

    private val viewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(BudgetViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity.setSupportActionBar(toolbar)
        with(activity.supportActionBar!!) {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            toolbarTitle.setText(R.string.edit_repartition)
        }

        viewModel.get(false).observe(this) {
            budgets = it
            spinner.adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, it.map { b -> b.name }.toTypedArray())
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit(commit = true) {
                    putLong("BUDGET_ID", budgets[position].id!!)
                }
                viewModel.getPart(budgets[position]).observeOnce(this@PieFragment) {
                    recyclerView.adapter = Adapter(it).apply {
                        progressChangeListener = ::updateChart
                        progressHasChangedListener = { p, _ ->
                            viewModel.save(p)
                        }
                    }
                    setUpChart(chart, it, recyclerView)
                }
            }

        }


    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> navController.navigateUp()
            else -> super.onOptionsItemSelected(item)
        }
    }

    class Adapter(val list: List<BudgetPart>) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        var progressChangeListener: ((BudgetPart, Float, Slider) -> Unit)? = null
        var progressHasChangedListener: ((BudgetPart, Float) -> Unit)? = null

        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_part_pie, parent, false))


        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val colorGenerator = ColorGenerator(holder.itemView.context)
            val color = colorGenerator.getColor(item.name + item.id)
            holder.itemView.color.setImageDrawable(
                    when (val d = holder.itemView.context.getDrawable(R.drawable.generic_circle_drawable)) {
                        is ShapeDrawable -> {
                            d.paint.color = color
                            d
                        }

                        is GradientDrawable -> {
                            d.setColor(color)
                            d
                        }

                        is ColorDrawable -> {
                            d.color = color
                            d
                        }

                        else -> throw IllegalStateException("Unknown drawable")
                    }
            )


            holder.itemView.percent.setText(String.format("%.0f%%", item.percent * 100), color)
            holder.itemView.name.text = item.name
            holder.itemView.slider.setLabelFormatter { (it * 100.0).format(0) + '%' }
            holder.itemView.slider.value = item.percent.toFloat()
            holder.itemView.slider.addOnChangeListener { slider, value, _ ->
                progressChangeListener?.invoke(item, value, slider)
            }
            holder.itemView.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {

                }

                override fun onStopTrackingTouch(slider: Slider) {
                    progressHasChangedListener?.invoke(item, slider.value)
                    holder.itemView.percent.setText(String.format("%.0f%%", slider.value * 100), color)
                }

            })
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment PieFragment.
         */
        @JvmStatic
        fun newInstance() =
                PieFragment().apply {
                    arguments = Bundle().apply {}
                }
    }


}