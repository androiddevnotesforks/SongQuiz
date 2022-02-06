package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ActivityQuizBinding
import com.aaronfodor.android.songquiz.view.utils.AppActivity
import com.aaronfodor.android.songquiz.view.utils.AppDialog
import com.aaronfodor.android.songquiz.view.utils.CrossFadeTransition
import com.aaronfodor.android.songquiz.view.utils.RequiredPermission
import com.aaronfodor.android.songquiz.viewmodel.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence

class QuizActivity : AppActivity(keepScreenAlive = true) {

    companion object{
        const val PLAYLIST_KEY = "playlist key"
    }

    private lateinit var binding: ActivityQuizBinding
    private lateinit var viewModel: QuizViewModel

    var imageSize = 0

    var userInputButtonAnimation: ObjectAnimator? = null
    var ttsButtonAnimation: ObjectAnimator? = null

    override var requiredPermissions: List<RequiredPermission> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requiredPermissions = listOf(
            RequiredPermission(Manifest.permission.INTERNET, getString(R.string.permission_internet), getString(R.string.permission_internet_explanation)),
            RequiredPermission(Manifest.permission.RECORD_AUDIO, getString(R.string.permission_record_audio), getString(R.string.permission_record_audio_explanation)),
            RequiredPermission(Manifest.permission.VIBRATE, getString(R.string.permission_vibrate), getString(R.string.permission_vibrate_explanation))
        )

        binding = ActivityQuizBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        playlistColorSetter(getColor(R.color.colorAccent))

        // get settings related to the quiz
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val songDuration = sharedPreferences.getInt(getString(R.string.SETTINGS_KEY_SONG_DURATION), this.resources.getInteger(R.integer.song_duration_sec_default))
        val repeatAllowed = sharedPreferences.getBoolean(getString(R.string.SETTINGS_KEY_REPEAT), this.resources.getBoolean(R.bool.repeat_default))
        val difficultyCompensation = sharedPreferences.getBoolean(getString(R.string.SETTINGS_KEY_DIFFICULTY_COMPENSATION), this.resources.getBoolean(R.bool.difficulty_compensation_default))
        val extendedInfoAllowed = sharedPreferences.getBoolean(getString(R.string.SETTINGS_KEY_EXTENDED_QUIZ_INFO), this.resources.getBoolean(R.bool.extended_quiz_info_default))

        // load ad
        val adRequest = AdRequest.Builder().build()
        binding.content.adQuiz.loadAd(adRequest)

        viewModel = ViewModelProvider(this).get(QuizViewModel::class.java)

        val playlistId = intent.extras?.getString(PLAYLIST_KEY) ?: ""
        viewModel.setPlaylistByIdAndSettings(playlistId, songDuration, repeatAllowed,
                                            difficultyCompensation, extendedInfoAllowed)
        viewModel.numProgressBarSteps = resources.getInteger(R.integer.progressbar_max_value)
        imageSize = resources.getDimension(R.dimen.game_image_pixels).toInt()
    }

    override fun onBackPressed() {
        val closeDialog = AppDialog(
            this, getString(R.string.exit_quiz),
            getString(R.string.exit_quiz_dialog), R.drawable.icon_question
        )

        closeDialog.setPositiveButton {
            viewModel.clearState()
            startActivity(Intent(this, MenuActivity::class.java))
        }
        closeDialog.show()
    }

    fun navigateToMainMenu() {
        viewModel.clearState()
        startActivity(Intent(this, MenuActivity::class.java))
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
                    userInputButtonAnimation?.cancel()
                    userInputButtonAnimation = tappableInfiniteAnimation(binding.content.userSpeechButton)
                    userInputButtonAnimation?.start()
                }
                UserInputState.DISABLED -> {
                    binding.content.userSpeechButton.setImageResource(R.drawable.icon_mic_off)
                    binding.content.userSpeechButton.isEnabled = false
                    userInputButtonAnimation?.cancel()
                    userInputButtonAnimation = tappableEndAnimation(binding.content.userSpeechButton)
                    userInputButtonAnimation?.start()
                }
                UserInputState.RECORDING -> {
                    binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform)
                    binding.content.userSpeechButton.isEnabled = true
                    userInputButtonAnimation?.cancel()
                    userInputButtonAnimation = tappableEndAnimation(binding.content.userSpeechButton)
                    userInputButtonAnimation?.start()
                }
                else -> {
                    binding.content.userSpeechButton.isEnabled = false
                    userInputButtonAnimation?.cancel()
                    userInputButtonAnimation = tappableEndAnimation(binding.content.userSpeechButton)
                    userInputButtonAnimation?.start()
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
                    ttsButtonAnimation?.cancel()
                    ttsButtonAnimation = tappableInfiniteAnimation(binding.content.ttsSpeechButton)
                    ttsButtonAnimation?.start()
                }
                TtsState.DISABLED -> {
                    binding.content.ttsSpeechButton.setImageResource(R.drawable.icon_sound_off)
                    binding.content.ttsSpeechButton.isEnabled = false
                    ttsButtonAnimation?.cancel()
                    ttsButtonAnimation = tappableEndAnimation(binding.content.ttsSpeechButton)
                    ttsButtonAnimation?.start()
                }
                TtsState.SPEAKING -> {
                    binding.content.ttsSpeechButton.setImageResource(R.drawable.icon_sound_speaking)
                    binding.content.ttsSpeechButton.isEnabled = false
                    ttsButtonAnimation?.cancel()
                    ttsButtonAnimation = tappableEndAnimation(binding.content.ttsSpeechButton)
                    ttsButtonAnimation?.start()
                }
                else -> {
                    binding.content.ttsSpeechButton.isEnabled = false
                    ttsButtonAnimation?.cancel()
                    ttsButtonAnimation = tappableEndAnimation(binding.content.ttsSpeechButton)
                    ttsButtonAnimation?.start()
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
                QuizUiState.EXIT -> {
                    navigateToMainMenu()
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
                    // specific, small image needed as thumbnail
                    .override(imageSize, imageSize)
                    .placeholder(R.drawable.icon_album)
                    .error(R.drawable.icon_album)

                Glide.with(this)
                    .load(uri)
                    .apply(options)
                    .listener(object : RequestListener<Drawable>{

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable,
                            model: Any?,
                            target: Target<Drawable>,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource.let {
                                val palette = Palette.from(resource.toBitmap()).generate()
                                val fallbackColor = getColor(R.color.colorAccent)
                                val playlistColor = palette.getMutedColor(fallbackColor)
                                viewModel.playlistPrimaryColor.postValue(playlistColor)
                            }
                            // explicit transition
                            target.onResourceReady(resource, CrossFadeTransition())
                            return true
                        }

                    })
                    .into(binding.content.ivPlaylist)
            }

        }
        viewModel.playlistImageUri.observe(this, playlistUriObserver)

        val playlistPrimaryColorObserver = Observer<Int> { color ->
            playlistColorSetter(color)
        }
        viewModel.playlistPrimaryColor.observe(this, playlistPrimaryColorObserver)

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

        val songPlayedProgressValueObserver = Observer<Int> { progress ->
            binding.content.songPlayProgressBar.progress = progress
        }
        viewModel.songPlayProgressValue.observe(this, songPlayedProgressValueObserver)

        val songPlayedProgressPercentageObserver = Observer<Float> { percentage ->
            val currentColorInt = ColorUtils.blendARGB(
                getColor(R.color.colorLight),
                getColor(R.color.colorActive),
                percentage)
            val currentColor = String.format("#%06X", 0xFFFFFF and currentColorInt)
            binding.content.songPlayProgressBar.progressTintList = ColorStateList.valueOf(Color.parseColor(currentColor))
        }
        viewModel.songPlayProgressPercentage.observe(this, songPlayedProgressPercentageObserver)

    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    override fun onboardingDialog(){
        val keyOnboardingFlag = getString(R.string.PREF_KEY_ONBOARDING_QUIZ_SHOWED)
        // get saved info from preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val onboardingFlag = sharedPreferences.getBoolean(keyOnboardingFlag, false)

        if(!onboardingFlag){
            MaterialTapTargetSequence().addPrompt(
                MaterialTapTargetPrompt.Builder(this)
                .setTarget(binding.content.userSpeechButton)
                .setPrimaryText(getString(R.string.onboarding_quiz_user_input))
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setBackgroundColour(getColor(R.color.colorOnboardingBackground))
                .setFocalColour(getColor(R.color.colorOnboardingFocal))
                .create()
        ).addPrompt(
                MaterialTapTargetPrompt.Builder(this)
                .setTarget(binding.content.ttsSpeechButton)
                .setPrimaryText(getString(R.string.onboarding_quiz_speech))
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setBackgroundColour(getColor(R.color.colorOnboardingBackground))
                .setFocalColour(getColor(R.color.colorOnboardingFocal))
                    .setPromptStateChangeListener { prompt, state ->
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                            // persist showed flag to preferences
                            with(sharedPreferences.edit()){
                                remove(keyOnboardingFlag)
                                putBoolean(keyOnboardingFlag, true)
                                apply()
                            }
                        }
                    }
                .create()
        ).show()
        }
    }

    private fun showInfo(infoType: QuizUiState){

        when(infoType){
            QuizUiState.ERROR_PLAYLIST_LOAD -> {
                val message = getString(R.string.error_listable_load)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.empty()
            }
            QuizUiState.ERROR_PLAY_SONG -> {
                val message = getString(R.string.error_play_song)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.play()
            }
            QuizUiState.ERROR_SPEAK_TO_USER -> {
                val message = getString(R.string.error_speak_to_user)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.play()
            }
            else -> {}
        }

    }

    private fun playlistColorSetter(colorInt: Int){
        val darkness = 1-(0.299*Color.red(colorInt) + 0.587*Color.green(colorInt) + 0.114*Color.blue(colorInt))/255
        val playlistColor = if(darkness < 0.35){
            // color is too light, replace with the accent
            getColor(R.color.colorAccent)
        }
        else{
            colorInt
        }

        val backgroundColor = getColor(R.color.colorBackground)
        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(playlistColor, backgroundColor))
        gradientDrawable.cornerRadius = 0F
        binding.root.background = gradientDrawable
        this.window.statusBarColor = playlistColor
    }

    private fun tappableInfiniteAnimation(viewToAnimate: View) : ObjectAnimator{
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.1f)
        val animation = ObjectAnimator.ofPropertyValuesHolder(viewToAnimate, scaleX, scaleY)
        animation.interpolator = FastOutSlowInInterpolator()
        animation.duration = 500L
        animation.repeatCount = ObjectAnimator.INFINITE
        animation.repeatMode = ObjectAnimator.REVERSE
        return animation
    }

    private fun tappableEndAnimation(viewToAnimate: View) : ObjectAnimator{
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.00f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.00f)
        val animation = ObjectAnimator.ofPropertyValuesHolder(viewToAnimate, scaleX, scaleY)
        animation.interpolator = FastOutSlowInInterpolator()
        animation.duration = 0L
        animation.repeatCount = 0
        animation.repeatMode = ObjectAnimator.RESTART
        return animation
    }

}