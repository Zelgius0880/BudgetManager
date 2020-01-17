package zelgius.com.ripplelongpress

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.skyfishjy.library.RippleBackground

class RippleLongPressView : RippleBackground {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        this.attrs = attrs
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var attrs: AttributeSet? = null
    private val longPressDelay: Int

    var boundaries: Rect? = null
    var clicked = false

    var onTap: Runnable

    var l: ((View)->Boolean)? = null


    fun setOnLongClickListener(l: (View)->Boolean) {
        super.setOnLongClickListener(l)
        this.l = l
    }

    var onLongPress = Runnable {
        clicked = true
        // Long Press
        l?.invoke(this)
        stopRippleAnimation()
    }

    init {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.RippleBackground,
                0, 0).apply {

            try {
                longPressDelay = getInt(R.styleable.RippleBackground_rb_duration, 3000) / 2
            } finally {
                recycle()
            }
        }

        onTap = Runnable {
            handler.postDelayed(onLongPress, longPressDelay - ViewConfiguration.getTapTimeout().toLong())
        }

        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    boundaries = Rect(view.left, view.top, view.right, view.bottom)
                    handler.postDelayed(onTap, ViewConfiguration.getTapTimeout().toLong())

                    startRippleAnimation()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(onLongPress)
                    handler.removeCallbacks(onTap)
                    if (!clicked) view.performClick()
                    clicked = false
                    stopRippleAnimation()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!boundaries!!.contains(view.left + event.x.toInt(), view.top + event.y.toInt())) {
                        handler.removeCallbacks(onLongPress)
                        handler.removeCallbacks(onTap)
                    }

                }
            }
            true
        }
    }
}