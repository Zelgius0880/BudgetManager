package zelgius.com.budgetmanager.fragments

import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.fragment_pie.*
import zelgius.com.budgetmanager.ColorGenerator
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.round
import kotlin.math.abs

open class ChartFragment: Fragment() {

    val map = mutableMapOf<BudgetPart, PieEntry>()
    lateinit var dataSet: PieDataSet
    private lateinit var colors: MutableList<Int>

     open fun setUpChart(chart: PieChart, list: List<BudgetPart>, recyclerView: RecyclerView? = null) {
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setExtraOffsets(5f, 10f, 5f, 5f)

        chart.dragDecelerationFrictionCoef = 0.95f

        //chart.setCenterTextTypeface(tfLight)
        //chart.centerText = generateCenterSpannableText()

        chart.isDrawHoleEnabled = true
        chart.setHoleColor(Color.WHITE)

        chart.setTransparentCircleColor(Color.WHITE)
        chart.setTransparentCircleAlpha(110)
        chart.setTransparentCircleAlpha(110)

        chart.holeRadius = 50f
        chart.transparentCircleRadius = 61f

        chart.setDrawCenterText(true)

        chart.rotationAngle = 0f
        // enable rotation of the chart by touch
        chart.isRotationEnabled = true
        chart.isHighlightPerTapEnabled = true

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        // chart.setUnit(" €");
// chart.setDrawUnitsInChart(true);
// add a selection listener

        chart.animateY(1400, Easing.EaseInOutQuad)

        // chart.spin(2000, 0, 360);
        val l = chart.legend
        l.isEnabled = false
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        // entry label styling
        //chart.setDrawEntryLabels(false)
        chart.setEntryLabelColor(Color.WHITE)
        //chart.setEntryLabelTypeface(tfRegular)
        chart.setEntryLabelTextSize(18f)

        val entries = mutableListOf<PieEntry>()
        colors = mutableListOf()
        var remaining = 1.0
        val colorGenerator = ColorGenerator(requireContext())
        list.forEachIndexed { _, budgetPart ->
            PieEntry(
                    if (budgetPart.percent < 0.0) 0f else budgetPart.percent.toFloat(),
                    if (budgetPart.percent == 0.0) "" else budgetPart.name,
                    budgetPart
            ).let {
                entries.add(it)
                colors.add(colorGenerator.getColor(budgetPart.name + budgetPart.id!!))
                map[budgetPart] = it
                remaining -= budgetPart.percent
            }
        }
        if (remaining > 0)
            PieEntry(remaining.toFloat()).let {
                entries.add(it)
            }

        colors.add(Color.WHITE)

        dataSet = PieDataSet(entries, "")
        dataSet.colors = colors


        dataSet.setDrawValues(false)
        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 1.5f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        //dataSet.isHighlightEnabled = false

        chart.data = PieData(dataSet)
        //chart.isEnabled = false
        chart.invalidate()

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {}

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val item = e?.data
                if (item is BudgetPart) {
                    val index = list.indexOf(item)
                    if(recyclerView != null)
                        (recyclerView.layoutManager as? LinearLayoutManager)?.smoothScrollToPosition(recyclerView, RecyclerView.State(), index)
                }
            }

        })

    }


    fun updateChart(part: BudgetPart, value: Float, slider: Slider) {

        //if value and [part.percent] are the same, there is no change and so, no update to do
        if (value == part.percent.toFloat()) return

        var total = 0.0
        part.percent = value.toDouble().round(3)
        map.forEach { (t, _) -> total += t.percent }

        if (total > 1f) {
            part.percent = (value - (total - 1))
            slider.value = abs(part.percent.toFloat().round(3))
            total = 1.0
        }

        map[part] = PieEntry(
                if (part.percent < 0.0) 0f else part.percent.toFloat(),
                if (part.percent == 0.0) "" else part.name,
                part
        )

        val list = map.map { it.value }.toMutableList()

        if (total < 1) {
            list.add(PieEntry(1 - total.toFloat()))
        }
        dataSet = PieDataSet(list, "")
        dataSet.colors = colors
        dataSet.sliceSpace = 1.5f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        dataSet.setDrawValues(false)
        chart.data = PieData(dataSet)
        chart.invalidate()
    }

}