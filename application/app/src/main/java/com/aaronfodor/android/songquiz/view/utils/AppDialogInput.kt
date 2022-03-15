package com.aaronfodor.android.songquiz.view.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.AppDialogInputBinding


/**
 * Dialog input class of the app
 *
 * @param    context            Context of the parent where the dialog is shown
 * @param    title              Title of the dialog
 * @param    description        Description of the dialog
 */
class AppDialogInput(context: Context, title: String, description: String) : AlertDialog(context) {

    private val binding: AppDialogInputBinding

    init {
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes?.windowAnimations = R.style.DialogAnimation

        binding = AppDialogInputBinding.inflate(LayoutInflater.from(context))
        setView(binding.root)

        binding.tvCustomDialogTitle.text = title
        binding.tvAppDialogDescription.text = description

        binding.btnPositiveAppDialog.setOnClickListener {
            this.dismiss()
        }
        binding.btnNegativeAppDialog.setOnClickListener {
            this.dismiss()
        }
    }

    /**
     * Sets the positive Button on click listener
     *
     * @param    func        Lambda to execute when the positive Button is pressed
     */
    fun setPositiveButton(func: (text: String) -> Unit){
        binding.btnPositiveAppDialog.setOnClickListener {
            this.dismiss()
            func(binding.itInput.text.toString())
        }
    }

    /**
     * Sets the negative Button on click listener
     *
     * @param    func        Lambda to execute when the negative Button is pressed
     */
    fun setNegativeButton(func: () -> Unit){
        binding.btnNegativeAppDialog.setOnClickListener {
            this.dismiss()
            func()
        }
    }

    override fun show() {
        super.show()
        binding.itInput.requestFocus()
        this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

}