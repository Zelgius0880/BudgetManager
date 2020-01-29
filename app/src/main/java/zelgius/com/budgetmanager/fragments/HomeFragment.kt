package zelgius.com.budgetmanager.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.view.ImplementsAlphaChart
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_part_main.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import zelgius.com.budgetmanager.ColorGenerator
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.dpToPx
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.observeOnce
import zelgius.com.budgetmanager.viewModel.BudgetViewModel
import java.time.Duration


/** A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : ChartFragment() {


    private val viewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(BudgetViewModel::class.java)
    }

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
                        else -> false
                    }
                }

        val id = PreferenceManager.getDefaultSharedPreferences(requireContext()).getLong("BUDGET_ID", -1L)

        if (id > 0) {
            viewModel.getPart(id, greaterThanZero = true).observeOnce(this) {
                recyclerView.adapter = Adapter(it)
                setUpChart(chart, it, recyclerView)
            }

            viewModel.get(id).observeOnce(this) {
                if (it != null)
                    name.text = it.name
            }
        }

    }

    inner class Adapter(val list: List<BudgetPart>) : RecyclerView.Adapter<ViewHolder>() {
        private val colorGenerator = ColorGenerator(requireContext())
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_part_main, parent, false))

        override fun getItemCount(): Int = list.size

        @ImplementsAlphaChart
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.itemView.partName.text = item.name
            holder.itemView.imageView.visibility = View.GONE
            holder.itemView.progress.apply {
                val color = colorGenerator.getColor(item.name + item.id)
                donutColors = intArrayOf(color)
                animation.duration = 1000L
                animate(listOf(101f))

                Handler().apply {
                    var current = 0.0
                    val i = item.goal / (1000.0 / 50.0) // a view is refreshed +- every 16ms in the best conditions. Dividing by 50 to let the time to the view to be refreshed

                    var work: (() -> Unit)? = null
                    work = {
                        holder.itemView.progressText.text = String.format("%.1f / %.1f", current, item.goal)

                        current += i
                        if (current < item.goal) postDelayed(50L, work!!)
                        else {
                            holder.itemView.progressText.text = String.format("%.1f / %.1f", item.goal, item.goal)
                            //holder.itemView.imageView.visibility = View.VISIBLE

                           holder.itemView.motion.transitionToEnd()
                        }

                    }
                    postDelayed(50L, work)
                }
            }

        }

    }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    class ResizeAnimation(var view: View, private val targetWidth: Int, private val startWidth: Int = view.width) : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            val newWidth = (startWidth + (targetWidth - startWidth) * interpolatedTime).toInt()
            view.layoutParams.width = newWidth
            view.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }


        fun startAnimation(duration: Long) {
            this.duration = duration
            startAnimation()
        }

        fun startAnimation() {
            view.layoutParams.width = startWidth
            view.requestLayout()

            view.startAnimation(this)
        }

    }
}