package zelgius.com.budgetmanager

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt


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

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, object  : Observer<T> {
        override fun onChanged(t: T) {
            work(t)
            removeObserver(this)
        }
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
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}
fun Float.round(decimals: Int): Float {
    var multiplier = 1.0f
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun TextView.setText(text: CharSequence, @ColorInt color: Int) {
    fun getLuminance(@ColorInt color: Int): Double {

        val array = doubleArrayOf(Color.red(color).toDouble(), Color.green(color).toDouble(), Color.blue(color).toDouble())
        for (i in array.indices) {
            array[i] = array[i] / 255.0
            if (array[i] <= 0.0392) {
                array[i] = array[i] / 12.92
            } else {
                array[i] = ((array[i] + 0.055) / 1.055).pow(2.4)
            }
        }
        return (299.0 * array[0] + 587.0 * array[1] + 114.0 * array[2]) / 1000.0
    }

    fun textShouldBeBlack(@ColorInt color: Int): Boolean {
        return getLuminance(color) > sqrt(0.052500000000000005) - 0.05
    }

    setText(text)
    if(textShouldBeBlack(color))
        setTextColor(Color.BLACK)
    else
        setTextColor(Color.WHITE)
}

fun  TextView.setText(@StringRes text: Int, @ColorInt color: Int)
= setText(context.getText(text), color)


/**
 *
 * public class Tools {
public static double getLuminance(String colorString) {
String colorString2 = colorString.replaceAll("#", "");
if (colorString2.length() == 8) {
colorString2 = colorString2.substring(2);
}
double[] color = {(double) Integer.parseInt(colorString2.substring(0, 2), 16), (double) Integer.parseInt(colorString2.substring(2, 4), 16), (double) Integer.parseInt(colorString2.substring(4), 16)};
for (int i = 0; i < color.length; i++) {
color[i] = color[i] / 255.0d;
if (color[i] <= 0.0392d) {
color[i] = color[i] / 12.92d;
} else {
color[i] = Math.pow((color[i] + 0.055d) / 1.055d, 2.4d);
}
}
return (((299.0d * color[0]) + (587.0d * color[1])) + (114.0d * color[2])) / 1000.0d;
}

public static boolean getTextShouldBeBlack(String colorString) {
return getLuminance(colorString) > Math.sqrt(0.052500000000000005d) - 0.05d;
}
}
 */
