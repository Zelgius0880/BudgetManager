package zelgius.com.budgetmanager.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_part_pie.view.*
import kotlinx.android.synthetic.main.fragment_pie.*
import zelgius.com.budgetmanager.*
import zelgius.com.budgetmanager.dialogs.PartDialog
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.viewModel.BudgetViewModel
import zelgius.com.swipetodelete.SwipeToDeletePagedAdapter
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 * Use the [PieFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PieFragment : ChartFragment() {

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

        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BudgetPart>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(old: BudgetPart,
                                         new: BudgetPart) = old.id == new.id

            override fun areContentsTheSame(old: BudgetPart,
                                            new: BudgetPart) =
                    old == new
        }
    }


    private val navController by lazy { findNavController() }
    private val activity by lazy { requireActivity() as AppCompatActivity }
    lateinit var budgets: List<Budget>
    private var needRefresh = true
    private lateinit var _view: View
    private var budget: Budget? = null
    private var listLiveData: LiveData<PagedList<BudgetPart>> ? = null

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

    private val adapter by lazy {
        Adapter { item ->
            needRefresh = true
            removePart(item)
            viewModel.delete(item).observeOnce(this) {
                Snackbar
                        .make(coordinator, R.string.item_removed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            needRefresh = true
                            item.id = null
                            viewModel.save(item)
                        }
                        //snackbar.setActionTextColor(Color.YELLOW)
                        .show()
            }
        }.apply {
            progressChangeListener = ::updateChart
            progressHasChangedListener = { p, value, slider ->
                viewModel.save(p)
                updateChart(p, value, slider)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _view = view
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            toolbarTitle.setText(R.string.edit_repartition)
        }

        viewModel.get(false).observe(this) {
            budgets = it
            spinner.adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, it.map { b -> b.name }.toTypedArray())
        }

        val adapter = adapter

        fab.setOnClickListener {
            showPartDialog(null)
        }

        recyclerView.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                budget = budgets[position]
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit(commit = true) {
                    putLong("BUDGET_ID", budgets[position].id!!)
                }
                needRefresh = true

                listLiveData?.removeObserver(listObserver)
                viewModel.getPartPagedList(budgets[position]).let {
                    it.observe(this@PieFragment, listObserver)
                    listLiveData = it
                }

            }

        }


    }

    val listObserver = Observer<PagedList<BudgetPart>> {
        adapter.submitList(it) {
            if (needRefresh) {
                setUpChart(chart, it, recyclerView)
                needRefresh = false
            }
        }
    }

    private fun showPartDialog(part: BudgetPart?) {
        PartDialog().apply {
            if (part != null)
                this.part = BudgetPart(part.id, part.percent, part.name, part.goal, part.closed, part.reached, part.refBudget)
            else this.part.refBudget = budget?.id

            budgets = this@PieFragment.budgets
            listener = { p, b ->
                viewModel.save(b, p).observe(this) {
                    if (it)
                        Snackbar.make(_view, R.string.save_ok, Snackbar.LENGTH_SHORT).show()
                }
            }
        }.show(parentFragmentManager, "dialog_part")
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> navController.navigateUp()
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class Adapter(deleteListener: (BudgetPart) -> Unit) : SwipeToDeletePagedAdapter<BudgetPart, Adapter.ViewHolder>(DIFF_CALLBACK, dragOnView = true, deleteListener = deleteListener) {
/*
        var progressChangeListener: ((BudgetPart, Float, Slider) -> Unit)? = null
        var progressHasChangedListener: ((BudgetPart, Float, Slider) -> Unit)? = null
*/

        var progressChangeListener: ((BudgetPart, Float, SeekBar) -> Boolean)? = null
        var progressHasChangedListener: ((BudgetPart, Float, SeekBar) -> Unit)? = null


        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_part_pie, parent, false))


        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)

            if (item != null) {
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

                if (holder.itemView is MaterialCardView) {
                    holder.itemView.isChecked = item.closed
                    holder.itemView.setOnLongClickListener {
                        holder.itemView.isChecked = !holder.itemView.isChecked
                        needRefresh = true
                        viewModel.close(item, !item.closed)
                        true
                    }

                    holder.itemView.setOnClickListener {
                        showPartDialog(item)
                    }
                }
                holder.itemView.percent.setText(String.format("%.0f%%", item.percent * 100), color)
                holder.itemView.name.text = item.name
                //holder.itemView.slider.setLabelFormatter { (it * 100.0).format(0) + '%' }
                holder.itemView.slider.max = 100
                holder.itemView.slider.progress = (item.percent.toFloat() * 100).roundToInt()
                holder.itemView.slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if(progressChangeListener?.invoke(item, progress / 100f, seekBar)== true)
                            holder.itemView.percent.setText(String.format("%d%%", seekBar.progress), color)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        progressHasChangedListener?.invoke(item, seekBar.progress / 100f, seekBar)
                        holder.itemView.percent.setText(String.format("%d%%", seekBar.progress), color)
                    }

                })

                holder.itemView.dragHook.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> startDrag(holder)
                        MotionEvent.ACTION_UP -> notifyItemChanged(holder.adapterPosition) // will reset the swipe
                    }
                    true
                }



                /*holder.itemView.slider.onChangeListener = Slider.OnChangeListener { slider, value, _ ->
                    progressChangeListener?.invoke(item, value, slider)
                }

                holder.itemView.slider.onSliderTouchListener = object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        progressHasChangedListener?.invoke(item, slider.value, slider)
                        holder.itemView.percent.setText(String.format("%.0f%%", slider.value * 100), color)
                    }

                }*/
            }
        }
    }


}