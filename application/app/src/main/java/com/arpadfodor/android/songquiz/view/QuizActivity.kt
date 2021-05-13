package com.arpadfodor.android.songquiz.view

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.ActivityQuizBinding
import com.arpadfodor.android.songquiz.view.utils.AppActivity
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.viewmodel.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar

class QuizActivity : AppActivity(screenAlive = true) {

    companion object{
        const val PLAYLIST_KEY = "playlist key"
    }

    private lateinit var binding: ActivityQuizBinding
    private lateinit var viewModel: QuizViewModel

    override var requiredPermissions = listOf(
        Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.VIBRATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuizBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // to make text views scrollable
        binding.content.tvInfo.movementMethod = ScrollingMovementMethod()
        binding.content.tvRecognition.movementMethod = ScrollingMovementMethod()

        // load ad
        val adRequest = AdRequest.Builder().build()
        binding.content.adQuiz.loadAd(adRequest)

        viewModel = ViewModelProvider(this).get(QuizViewModel::class.java)

        val playlistId = intent.extras?.getString(PLAYLIST_KEY) ?: ""
        viewModel.setPlaylistById(playlistId)
    }

    override fun onBackPressed() {
        val closeDialog = AppDialog(
            this, getString(R.string.exit_quiz),
            getString(R.string.exit_quiz_dialog), R.drawable.icon_question
        )

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

        val quizStateObserver = Observer<QuizUiState> { state ->
            when(state){
                QuizUiState.LOADING -> {
                    binding.content.progressBar.visibility = View.VISIBLE
                    binding.content.tvQuizStatus.text = ""
                }
                QuizUiState.READY_TO_START -> {
                    binding.content.progressBar.visibility = View.GONE
                    binding.content.tvQuizStatus.text = getString(R.string.ready_to_start)
                }
                QuizUiState.PLAY -> {
                    binding.content.progressBar.visibility = View.GONE
                    binding.content.tvQuizStatus.text = ""
                }
                QuizUiState.ERROR_PLAYLIST_LOAD -> {
                    binding.content.progressBar.visibility = View.GONE
                    binding.content.tvQuizStatus.text =
                        getString(R.string.error_playlist_load_description)
                    showError(QuizUiState.ERROR_PLAYLIST_LOAD)
                }
                QuizUiState.ERROR_PLAY_SONG -> {
                    binding.content.progressBar.visibility = View.GONE
                    binding.content.tvQuizStatus.text = ""
                    showError(QuizUiState.ERROR_PLAY_SONG)
                }
                QuizUiState.ERROR_SPEAK_TO_USER -> {
                    binding.content.progressBar.visibility = View.GONE
                    binding.content.tvQuizStatus.text = ""
                    showError(QuizUiState.ERROR_SPEAK_TO_USER)
                }
                else -> {}
            }
        }
        viewModel.quizUiState.observe(this, quizStateObserver)

        val playlistUriObserver = Observer<String> { uri ->
            if(uri.isEmpty()){
                binding.content.ivPlaylist.setImageResource(R.drawable.error)
            }
            else{
                val options = RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.icon_album)
                    .error(R.drawable.error)
                Glide.with(this).load(uri).apply(options).into(binding.content.ivPlaylist)
            }
        }
        viewModel.playlistImageUri.observe(this, playlistUriObserver)

        val infoObserver = Observer<String> { info ->

            val view = binding.content.tvInfo

            if(info.isBlank() && view.text.isNotBlank()){
                AnimationUtils.loadAnimation(this, R.anim.slide_out_left).also {
                    view.startAnimation(it)
                    view.visibility = View.INVISIBLE
                }
            }
            else if(info.isNotBlank()){
                AnimationUtils.loadAnimation(this, R.anim.slide_in_left).also {
                    view.startAnimation(it)
                    view.visibility = View.VISIBLE
                }
            }

            binding.content.tvInfo.text = info
        }
        viewModel.info.observe(this, infoObserver)

        val recognitionObserver = Observer<String> { recognition ->

            val view = binding.content.tvRecognition

            if(recognition.isBlank() && view.text.isNotBlank()){
                AnimationUtils.loadAnimation(this, R.anim.slide_out_right).also {
                    view.startAnimation(it)
                    view.visibility = View.INVISIBLE
                }
            }
            else if(recognition.isNotBlank() && view.text.isBlank()){
                AnimationUtils.loadAnimation(this, R.anim.slide_in_right).also {
                    view.startAnimation(it)
                    view.visibility = View.VISIBLE
                }
            }

            binding.content.tvRecognition.text = recognition
        }
        viewModel.recognition.observe(this, recognitionObserver)

        val songPlayedProgressObserver = Observer<Int> { progress ->
            if(progress > 0){
                binding.content.progressBarSongPlayed.visibility = View.VISIBLE
                binding.content.progressBarSongPlayed.progress = progress
            }
            else{
                binding.content.progressBarSongPlayed.visibility = View.INVISIBLE
            }
        }
        viewModel.songPlayProgress.observe(this, songPlayedProgressObserver)

    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun showError(errorType: QuizUiState){

        when(errorType){
            QuizUiState.ERROR_PLAYLIST_LOAD -> {
                val errorMessage = getString(R.string.error_playlist_load)
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            }
            QuizUiState.ERROR_PLAY_SONG -> {
                val errorMessage = getString(R.string.error_play_song)
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                viewModel.quizUiState.postValue(QuizUiState.PLAY)
            }
            QuizUiState.ERROR_SPEAK_TO_USER -> {
                val errorMessage = getString(R.string.error_speak_to_user)
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                viewModel.quizUiState.postValue(QuizUiState.PLAY)
            }
            else -> {}
        }

    }

}