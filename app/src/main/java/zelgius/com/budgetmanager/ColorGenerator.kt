package zelgius.com.budgetmanager

import android.content.Context
import kotlin.math.abs

class ColorGenerator(val context: Context) {
    val color by lazy { context.resources.getIntArray(R.array.rainbow) }

    fun getColor(text: String) = color[abs(text.hashCode()) % color.size]
}