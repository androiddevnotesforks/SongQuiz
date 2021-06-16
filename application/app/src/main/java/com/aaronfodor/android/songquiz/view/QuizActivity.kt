package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ActivityQuizBinding
import com.aaronfodor.android.songquiz.view.utils.AppActivity
import com.aaronfodor.android.songquiz.view.utils.AppDialog
import com.aaronfodor.android.songquiz.viewmodel.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar

class QuizActivity : AppActivity(keepScreenAlive = true) {

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

        // get settings related to the quiz
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val repeatAllowed = sharedPreferences.getBoolean(getString(R.string.SETTINGS_KEY_REPEAT),
            this.resources.getBoolean(R.bool.repeat_default))
        val songDuration = sharedPreferences.getInt(getString(R.string.SETTINGS_KEY_SONG_DURATION),
            this.resources.getInteger(R.integer.song_duration_sec_default))

        // load ad
        val adRequest = AdRequest.Builder().build()
        binding.content.adQuiz.loadAd(adRequest)

        viewModel = ViewModelProvider(this).get(QuizViewModel::class.java)

        val playlistId = intent.extras?.getString(PLAYLIST_KEY) ?: ""
        viewModel.setPlaylistByIdAndSettings(playlistId, repeatAllowed, songDuration)
        viewModel.numProgressSteps = resources.getInteger(R.integer.progressbar_max_value)
    }

    override fun onBackPressed() {
        val closeDialog = AppDialog(
            this, getString(R.string.exit_quiz),
            getString(R.string.exit_quiz_dialog), R.drawable.icon_question
        )

        closeDialog.setPositiveButton {
            startActivity(Intent(this, MenuActivity::class.java))
            viewModel.clearState()
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
                    binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform)
                    binding.content.userSpeechButton.isEnabled = true
                }
                else -> {
                    binding.content.userSpeechButton.isEnabled = false
                }
            }
        }
        viewModel.userInputState.observe(this, userInputStateObserver)

        val userInputdBStateObserver = Observer<RmsState> { state ->
            if(viewModel.userInputState.value == UserInputState.RECORDING){
                when(state){
                    RmsState.LEVEL7 -> {
                        binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform_7)
                    }
                    RmsState.LEVEL6 -> {
                        binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform_6)
                    }
                    RmsState.LEVEL5 -> {
                        binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform_5)
                    }
                    RmsState.LEVEL4 -> {
                        binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform_4)
                    }
                    RmsState.LEVEL3 -> {
                        binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform_3)
                    }
                    RmsState.LEVEL2 -> {
                        binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform_2)
                    }
                    RmsState.LEVEL1 -> {
                        binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform_1)
                    }
                    else -> {
                        binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform)
                    }
                }
            }
        }
        viewModel.rmsState.observe(this, userInputdBStateObserver)

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

            if(state != QuizUiState.LOADING){
                binding.content.loadIndicatorProgressBar.visibility = View.GONE
            }

            when(state){
                QuizUiState.LOADING -> {
                    binding.content.loadIndicatorProgressBar.visibility = View.VISIBLE
                }
                QuizUiState.READY_TO_START -> {
                    viewModel.info.value = getString(R.string.ready_to_start)
                }
                QuizUiState.ERROR_PLAYLIST_LOAD -> {
                    viewModel.info.value = getString(R.string.error_playlist_load_description)
                    showInfo(QuizUiState.ERROR_PLAYLIST_LOAD)
                }
                QuizUiState.ERROR_PLAY_SONG -> {
                    viewModel.info.value = getString(R.string.error_play_song_description)
                    showInfo(QuizUiState.ERROR_PLAY_SONG)
                }
                QuizUiState.ERROR_SPEAK_TO_USER -> {
                    viewModel.info.value = getString(R.string.error_speak_to_user_description)
                    showInfo(QuizUiState.ERROR_SPEAK_TO_USER)
                }
                else -> {}
            }
        }
        viewModel.uiState.observe(this, quizStateObserver)

        val playlistUriObserver = Observer<String> { uri ->
            if(uri.isEmpty()){
                binding.content.ivPlaylist.setImageResource(R.drawable.icon_album)
            }
            else{
                val options = RequestOptions()
                    .centerCrop()
                    // better image quality: 4 bytes per pixel
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .placeholder(R.drawable.icon_album)
                    .error(R.drawable.icon_album)

                Glide.with(this)
                    .load(uri)
                    .apply(options)
                    .into(binding.content.ivPlaylist)
            }
        }
        viewModel.playlistImageUri.observe(this, playlistUriObserver)

        val infoObserver = Observer<String> { info ->
            val view = binding.content.svInfo

            if(info.isBlank() && binding.content.tvInfo.text.isNotBlank()){
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
            // scroll to the bottom of the scrollview
            view.scrollTo(0, view.bottom)
        }
        viewModel.info.observe(this, infoObserver)

        val recognitionObserver = Observer<String> { recognition ->
            val view = binding.content.svRecognition

            if(recognition.isBlank() && binding.content.tvRecognition.text.isNotBlank()){
                AnimationUtils.loadAnimation(this, R.anim.slide_out_right).also {
                    view.startAnimation(it)
                    view.visibility = View.INVISIBLE
                }
            }
            else if(recognition.isNotBlank() && binding.content.tvRecognition.text.isBlank()){
                AnimationUtils.loadAnimation(this, R.anim.slide_in_right).also {
                    view.startAnimation(it)
                    view.visibility = View.VISIBLE
                }
            }

            binding.content.tvRecognition.text = recognition
            // scroll to the bottom of the scrollview
            view.scrollTo(0, view.bottom)
        }
        viewModel.recognition.observe(this, recognitionObserver)

        val songPlayedProgressObserver = Observer<Int> { progress ->
            if(progress > 0){
                binding.content.songPlayProgressBar.visibility = View.VISIBLE
                binding.content.songPlayProgressBar.progress = progress
            }
            else{
                binding.content.songPlayProgressBar.visibility = View.INVISIBLE
            }
        }
        viewModel.songPlayProgress.observe(this, songPlayedProgressObserver)

    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun showInfo(infoType: QuizUiState){

        when(infoType){
            QuizUiState.ERROR_PLAYLIST_LOAD -> {
                val message = getString(R.string.error_playlist_load)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.uiState.postValue(QuizUiState.EMPTY)
            }
            QuizUiState.ERROR_PLAY_SONG -> {
                val message = getString(R.string.error_play_song)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.uiState.postValue(QuizUiState.PLAY)
            }
            QuizUiState.ERROR_SPEAK_TO_USER -> {
                val message = getString(R.string.error_speak_to_user)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.uiState.postValue(QuizUiState.PLAY)
            }
            else -> {}
        }

    }

}