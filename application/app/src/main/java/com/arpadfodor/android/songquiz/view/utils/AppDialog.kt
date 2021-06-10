package com.arpadfodor.android.songquiz.view.utils

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.AppDialogBinding

/**
 * Dialog class of the app
 *
 * @param    context            Context of the parent where the dialog is shown
 * @param    title              Title of the dialog
 * @param    description        Description of the dialog
 * @param    imageResourceCode  Image resource code shown on the dialog
 */
class AppDialog(context: Context, title: String, description: String, imageResourceCode: Int) : AlertDialog(context) {

    private val binding: AppDialogBinding

    init {
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes?.windowAnimations = R.style.DialogAnimation

        binding = AppDialogBinding.inflate(LayoutInflater.from(context))
        setView(binding.root)

        val image = ContextCompat.getDrawable(context, imageResourceCode)
        binding.ivAppDialog.setImageDrawable(image)

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
    fun setPositiveButton(func: () -> Unit){
        binding.btnPositiveAppDialog.setOnClickListener {
            this.dismiss()
            func()
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

}