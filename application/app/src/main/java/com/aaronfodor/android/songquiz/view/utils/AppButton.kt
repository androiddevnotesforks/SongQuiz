package com.aaronfodor.android.songquiz.view.utils

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.aaronfodor.android.songquiz.R

/**
 * Custom Button of the app - can be inherited from
 */
open class AppButton : AppCompatButton {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        this.background = ContextCompat.getDrawable(context, R.drawable.app_button)
        this.gravity = Gravity.CENTER
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.button_text_size))
        this.setTextColor(context.getColor(R.color.colorButtonText))

        this.setOnClickListener {
        }
    }

    /**
     * Sets the Button on click listener
     *
     * @param    func        Lambda to execute when the Button is pressed
     */
    fun setOnClickEvent(func: () -> Unit){
        this.setOnClickListener {
            func()
        }
    }

}