package zelgius.com.budgetmanager

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlin.math.roundToInt


@ColorInt
fun Context.getColor(@ColorRes color: Int, alpha: Float) =
        getColor(color).let {
            Color.argb(
                    (Color.alpha(color) * alpha).roundToInt(),
                    Color.red(it),
                    Color.green(it),
                    Color.blue(it))
        }

/**
 *
 * Get the value of dp to Pixel according to density of the screen
 *
 * @receiver Context
 * @param dp Float      the value in dp
 * @return the value of dp to Pixel according to density of the screen
 */
fun Context.dpToPx(dp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, Observer {
        work(it)
    })
}

fun AlertDialog.setListeners(positiveListener: (() -> Boolean)? = null, negativeListener: (() -> Boolean)? = null) {

    setOnShowListener {
        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (positiveListener == null) dismiss()
            else if (positiveListener()) dismiss()
        }

        getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            if (negativeListener == null) dismiss()
            else if (negativeListener()) dismiss()
        }
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)
