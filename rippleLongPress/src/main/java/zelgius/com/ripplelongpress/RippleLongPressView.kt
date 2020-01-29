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
    var clickListener: ((View)->Unit)? = null

    var isLocked: Boolean = false

    fun setOnLongClickListener(l: (View)->Boolean) {
        super.setOnLongClickListener(l)
        this.l = l
    }

    fun setOnClickListener(l: (View)->Unit) {
        super.setOnClickListener(l)
        clickListener = l
    }

    private var onLongPress = Runnable {
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
                    if(!isLocked) {
                        boundaries = Rect(view.left, view.top, view.right, view.bottom)
                        handler.postDelayed(onTap, ViewConfiguration.getTapTimeout().toLong())

                        startRippleAnimation()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(onLongPress)
                    handler.removeCallbacks(onTap)
                    if (!clicked){
                        //view.performClick()

                    }
                    clicked = false
                    stopRippleAnimation()
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!boundaries!!.contains(view.left + event.x.toInt(), view.top + event.y.toInt())) {
                        handler.removeCallbacks(onLongPress)
                        handler.removeCallbacks(onTap)
                        true
                    } else {
                        handler.removeCallbacks(onLongPress)
                        handler.removeCallbacks(onTap)
                        if (!clicked) view.performClick()
                        clicked = false
                        stopRippleAnimation()
                        false
                    }
                }
                else -> false
            }
        }
    }
}