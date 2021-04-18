package com.arpadfodor.android.songquiz.view.utils

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.arpadfodor.android.songquiz.R
import com.google.android.material.textfield.TextInputEditText

/**
 * Dialog class of the app
 *
 * @param    context            Context of the parent where the dialog is shown
 * @param    title              Title of the dialog
 * @param    description        Description of the dialog
 */
class AppInputDialog(context: Context, title: String, description: String) : AlertDialog(context) {

    /**
     * Positive and negative Buttons of the dialog
     */
    private var buttonPositive: AppPositiveButton
    private var buttonNegative: AppButton
    private var inputText: TextInputEditText

    init {

        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes?.windowAnimations = R.style.DialogAnimation

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.app_input_dialog, null)
        setView(view)

        val textViewTitle = view.findViewById<TextView>(R.id.tvCustomDialogTitle)
        textViewTitle.text = title

        val textViewDescription = view.findViewById<TextView>(R.id.tvAppDialogDescription)
        textViewDescription.text = description

        buttonPositive = view.findViewById(R.id.btnPositiveAppDialog)
        buttonPositive.setOnClickListener {
            this.dismiss()
        }

        buttonNegative = view.findViewById(R.id.btnNegativeAppDialog)
        buttonNegative.setOnClickListener {
            this.dismiss()
        }

        inputText = view.findViewById(R.id.itInput)

    }

    /**
     * Sets the positive Button on click listener
     *
     * @param    func        Lambda to execute when the positive Button is pressed
     */
    fun setPositiveButton(func: (text: String) -> Unit){
        buttonPositive.setOnClickListener {
            this.dismiss()
            func(inputText.text.toString())
        }
    }

    /**
     * Sets the negative Button on click listener
     *
     * @param    func        Lambda to execute when the negative Button is pressed
     */
    fun setNegativeButton(func: () -> Unit){
        buttonNegative.setOnClickListener {
            this.dismiss()
            func()
        }
    }

}