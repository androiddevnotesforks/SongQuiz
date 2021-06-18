package com.aaronfodor.android.songquiz.view.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.aaronfodor.android.songquiz.view.AuthActivity

class AuthRequestContract : ActivityResultContract<Unit, Boolean>() {

    companion object {
        const val FOR_RESULT_AUTH_SCREEN_KEY = "for result auth screen"
        const val IS_AUTH_SUCCESS_KEY = "is auth success"
    }

    /** Create an intent that can be used for [Activity.startActivityForResult]  */
    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(context, AuthActivity::class.java).apply {
            putExtra(FOR_RESULT_AUTH_SCREEN_KEY, true)
        }
    }

    /** Convert result obtained from [Activity.onActivityResult] to O  */
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return intent?.getBooleanExtra(IS_AUTH_SUCCESS_KEY, false) ?: false
    }

}