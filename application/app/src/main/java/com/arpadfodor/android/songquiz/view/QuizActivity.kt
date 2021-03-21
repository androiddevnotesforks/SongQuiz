package com.arpadfodor.android.songquiz.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.ActivityQuizBinding
import com.arpadfodor.android.songquiz.view.utils.AppActivity
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.viewmodel.QuizViewModel
import com.arpadfodor.android.songquiz.viewmodel.TtsState
import com.arpadfodor.android.songquiz.viewmodel.UserInputState

class QuizActivity : AppActivity(screenAlive = true) {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var viewModel: QuizViewModel

    private val recordAudioRequestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuizBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.quizToolbar)

        viewModel = ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    override fun onBackPressed() {

        val closeDialog = AppDialog(this, getString(R.string.exit_quiz),
            getString(R.string.exit_quiz_dialog), R.drawable.icon_question)

        closeDialog.setPositiveButton {
            startActivity(Intent(this, MainActivity::class.java))
            viewModel.clearQuizState()
        }
        closeDialog.show()
    }

    override fun appearingAnimations() {}

    override fun permissionCheck() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioRequestCode)
        }
    }

    override fun subscribeViewModel() {

        binding.quizContent.userSpeechButton.setOnClickListener {
            viewModel.getUserInput()
        }

        binding.quizContent.ttsSpeechButton.setOnClickListener {
            viewModel.speakToUser("Welcome to song quiz! How many player wants to play?")
        }

        val userInputStateObserver = Observer<UserInputState> { state ->
            when(state){
                UserInputState.ENABLED -> {
                    binding.quizContent.userSpeechButton.setImageResource(R.drawable.icon_mic_on)
                    binding.quizContent.userSpeechButton.isEnabled = true
                }
                UserInputState.DISABLED -> {
                    binding.quizContent.userSpeechButton.setImageResource(R.drawable.icon_mic_off)
                    binding.quizContent.userSpeechButton.isEnabled = false
                }
                UserInputState.RECORDING -> {
                    binding.quizContent.userSpeechButton.setImageResource(R.drawable.icon_mic_rec)
                    binding.quizContent.userSpeechButton.isEnabled = true
                }
                else -> {
                    binding.quizContent.userSpeechButton.isEnabled = false
                }
            }
        }
        viewModel.userInputState.observe(this, userInputStateObserver)

        val ttsStateObserver = Observer<TtsState> { state ->
            when(state){
                TtsState.ENABLED -> {
                    binding.quizContent.ttsSpeechButton.setImageResource(R.drawable.icon_listen)
                    binding.quizContent.ttsSpeechButton.isEnabled = true
                }
                TtsState.DISABLED -> {
                    binding.quizContent.ttsSpeechButton.setImageResource(R.drawable.icon_listen)
                    binding.quizContent.ttsSpeechButton.isEnabled = false
                }
                TtsState.SPEAKING -> {
                    binding.quizContent.ttsSpeechButton.setImageResource(R.drawable.icon_hourglass)
                    binding.quizContent.ttsSpeechButton.isEnabled = false
                }
                else -> {
                    binding.quizContent.ttsSpeechButton.isEnabled = false
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)

        val numListeningObserver = Observer<Int> { numListening ->
            binding.quizContent.tvQuizCntr.text = numListening.toString()
        }
        viewModel.numListening.observe(this, numListeningObserver)

        val recognitionObserver = Observer<String> { recognition ->
            binding.quizContent.tvRecognition.text = recognition
        }
        viewModel.recognition.observe(this, recognitionObserver)

    }

    override fun unsubscribeViewModel() {}

}