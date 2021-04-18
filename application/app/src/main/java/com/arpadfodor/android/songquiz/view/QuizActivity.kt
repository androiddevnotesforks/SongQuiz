package com.arpadfodor.android.songquiz.view

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.ActivityQuizBinding
import com.arpadfodor.android.songquiz.view.utils.AppActivity
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.viewmodel.PlaylistState
import com.arpadfodor.android.songquiz.viewmodel.QuizViewModel
import com.arpadfodor.android.songquiz.viewmodel.TtsState
import com.arpadfodor.android.songquiz.viewmodel.UserInputState

class QuizActivity : AppActivity(screenAlive = true) {

    companion object{
        const val PLAYLIST_KEY = "playlist key"
    }

    private lateinit var binding: ActivityQuizBinding
    private lateinit var viewModel: QuizViewModel

    override var requiredPermissions = listOf(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuizBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.quizToolbar)

        viewModel = ViewModelProvider(this).get(QuizViewModel::class.java)

        val playlistId = intent.extras?.getString(PLAYLIST_KEY) ?: ""
        viewModel.setPlaylistById(playlistId)
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

    override fun subscribeViewModel() {

        binding.content.userSpeechButton.setOnClickListener {
            viewModel.getUserInput()
        }

        binding.content.ttsSpeechButton.setOnClickListener {
            viewModel.infoToUser(clearUserInputText = true)
        }

        val userInputStateObserver = Observer<UserInputState> { state ->
            when(state){
                UserInputState.ENABLED -> {
                    binding.content.userSpeechButton.setImageResource(R.drawable.icon_mic_on)
                    binding.content.userSpeechButton.isEnabled = true
                }
                UserInputState.DISABLED -> {
                    binding.content.userSpeechButton.setImageResource(R.drawable.icon_mic_off)
                    binding.content.userSpeechButton.isEnabled = false
                }
                UserInputState.RECORDING -> {
                    binding.content.userSpeechButton.setImageResource(R.drawable.icon_mic_rec)
                    binding.content.userSpeechButton.isEnabled = true
                }
                else -> {
                    binding.content.userSpeechButton.isEnabled = false
                }
            }
        }
        viewModel.userInputState.observe(this, userInputStateObserver)

        val ttsStateObserver = Observer<TtsState> { state ->
            when(state){
                TtsState.ENABLED -> {
                    binding.content.ttsSpeechButton.setImageResource(R.drawable.icon_sound_on)
                    binding.content.ttsSpeechButton.isEnabled = true
                }
                TtsState.DISABLED -> {
                    binding.content.ttsSpeechButton.setImageResource(R.drawable.icon_sound_off)
                    binding.content.ttsSpeechButton.isEnabled = false
                }
                TtsState.SPEAKING -> {
                    binding.content.ttsSpeechButton.setImageResource(R.drawable.icon_sound_speaking)
                    binding.content.ttsSpeechButton.isEnabled = false
                }
                else -> {
                    binding.content.ttsSpeechButton.isEnabled = false
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)

        val playlistStateObserver = Observer<PlaylistState> { state ->
            val text = when(state){
                PlaylistState.READY -> {
                    getString(R.string.init_ready)
                }
                PlaylistState.LOADING -> {
                    getString(R.string.init_loading)
                }
                PlaylistState.ERROR -> {
                    getString(R.string.init_error)
                }
                else -> {
                    getString(R.string.init_error)
                }
            }
            binding.content.tvQuizCounter.text = text
        }
        viewModel.playlistState.observe(this, playlistStateObserver)

        val numListeningObserver = Observer<Int> { numListening ->
            binding.content.tvQuizCounter.text = numListening.toString()
        }
        viewModel.numListening.observe(this, numListeningObserver)


        val infoObserver = Observer<String> { info ->
            binding.content.tvInfo.text = info
        }
        viewModel.info.observe(this, infoObserver)

        val recognitionObserver = Observer<String> { recognition ->
            binding.content.tvRecognition.text = recognition
        }
        viewModel.recognition.observe(this, recognitionObserver)

    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

}