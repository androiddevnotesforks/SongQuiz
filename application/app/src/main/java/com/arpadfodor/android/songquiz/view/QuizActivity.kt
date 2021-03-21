package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.os.Bundle
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.view.utils.AppActivity
import com.arpadfodor.android.songquiz.view.utils.AppDialog

class QuizActivity : AppActivity(screenAlive = true) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        setSupportActionBar(findViewById(R.id.quizToolbar))
    }

    override fun onBackPressed() {

        val closeDialog = AppDialog(this, getString(R.string.exit_quiz),
            getString(R.string.exit_quiz_dialog), R.drawable.icon_question)
        closeDialog.setPositiveButton {
            startActivity(Intent(this, MainActivity::class.java))
        }
        closeDialog.show()

    }

    override fun appearingAnimations() {}
    override fun subscribeUI() {}
    override fun unsubscribeUI() {}

}