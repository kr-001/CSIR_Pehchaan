package com.app.csir_npl
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText

class CustomEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Call performClick when the touch event occurs
        if (event.action == MotionEvent.ACTION_DOWN) {
            performClick()
        }
        return super.onTouchEvent(event)
    }
}

