package zelgius.com.budgetmanager.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.slider.Slider

class MySlider : Slider {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var onChangeListener: OnChangeListener? = null
        set(value) {
            if (field != null) removeOnChangeListener(field!!)
            field = value
            addOnChangeListener(value)
        }

    var onSliderTouchListener: OnSliderTouchListener? = null
        set(value) {
            if (field != null) removeOnSliderTouchListener(field!!)
            field = value

            if (value != null)
                addOnSliderTouchListener(value)
        }

}
