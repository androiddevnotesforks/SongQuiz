package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ActivityQuizBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelGuessItem
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelQuizState
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelEndFeedback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence

class QuizActivity : AppActivity(keepScreenAlive = true) {

    companion object{
        const val PLAYLIST_KEY = "playlist key"
        const val PLAYLIST_FALLBACK_ID = "invalid id"
    }

    private lateinit var binding: ActivityQuizBinding
    override lateinit var viewModel: QuizViewModel

    var imageSize = 0

    var userInputButtonAnimation: ObjectAnimator? = null
    var ttsButtonAnimation: ObjectAnimator? = null
    var typeInputButtonAnimation: ObjectAnimator? = null

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

        viewModel = ViewModelProvider(this)[QuizViewModel::class.java]

        // try to see whether explicit intent can be parsed
        var playlistId = parseExplicitIntent()
        if(playlistId.isBlank()){
            // if not, parse implicit intent
            playlistId = parseImplicitIntent()
        }
        if(playlistId.isBlank()){
            // if still no playlist Id, revert to a fallback Id
            playlistId = PLAYLIST_FALLBACK_ID
        }

        viewModel.setPlaylistAndSettings(playlistId, songDuration, repeatAllowed, difficultyCompensation, extendedInfoAllowed)
        viewModel.numProgressBarSteps = resources.getInteger(R.integer.progressbar_max_value)
        imageSize = resources.getDimension(R.dimen.game_image_pixels).toInt()
    }

    private fun parseExplicitIntent(): String {
        return intent.extras?.getString(PLAYLIST_KEY) ?: ""
    }

    private fun parseImplicitIntent() : String{
        var playlistId = ""

        if(intent.type == "text/plain"){
            var text = intent.clipData?.getItemAt(0)?.text.toString()
            text = text.removePrefix("https://open.spotify.com/playlist/")
            text = text.replaceAfter("?", "")
            text = text.replace("?", "")
            playlistId = text
        }

        return playlistId
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
                    userInputButtonAnimation = binding.content.userSpeechButton.tappableInfiniteAnimation()
                    userInputButtonAnimation?.start()
                }
                UserInputState.DISABLED -> {
                    binding.content.userSpeechButton.setImageResource(R.drawable.icon_mic_off)
                    binding.content.userSpeechButton.isEnabled = false
                    userInputButtonAnimation?.cancel()
                    userInputButtonAnimation = binding.content.userSpeechButton.tappableEndAnimation()
                    userInputButtonAnimation?.start()
                }
                UserInputState.RECORDING -> {
                    binding.content.userSpeechButton.setImageResource(R.drawable.icon_waveform)
                    binding.content.userSpeechButton.isEnabled = true
                    userInputButtonAnimation?.cancel()
                    userInputButtonAnimation = binding.content.userSpeechButton.tappableEndAnimation()
                    userInputButtonAnimation?.start()
                }
                else -> {
                    binding.content.userSpeechButton.isEnabled = false
                    userInputButtonAnimation?.cancel()
                    userInputButtonAnimation = binding.content.userSpeechButton.tappableEndAnimation()
                    userInputButtonAnimation?.start()
                }
            }

            when(state){
                UserInputState.RECORDING, UserInputState.ENABLED -> {
                    binding.content.typeInputButton.let {
                        it.setOnClickListener {
                            viewModel.cancelUserInputJob()
                            showTypeInputDialog()
                        }
                        it.appear(R.anim.slide_in_bottom)
                    }
                }
                else -> {
                    binding.content.typeInputButton.let {
                        it.setOnClickListener {}
                        it.disappear(R.anim.slide_out_bottom)
                    }
                }
            }

            if(state == UserInputState.ENABLED){
                typeInputButtonAnimation?.cancel()
                typeInputButtonAnimation = binding.content.typeInputButton.tappableInfiniteAnimation()
                typeInputButtonAnimation?.start()
            }
            else{
                typeInputButtonAnimation?.cancel()
                typeInputButtonAnimation = binding.content.typeInputButton.tappableEndAnimation()
                typeInputButtonAnimation?.start()
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
                    ttsButtonAnimation = binding.content.ttsSpeechButton.tappableInfiniteAnimation()
                    ttsButtonAnimation?.start()
                }
                TtsState.DISABLED -> {
                    binding.content.ttsSpeechButton.setImageResource(R.drawable.icon_sound_off)
                    binding.content.ttsSpeechButton.isEnabled = false
                    ttsButtonAnimation?.cancel()
                    ttsButtonAnimation = binding.content.ttsSpeechButton.tappableEndAnimation()
                    ttsButtonAnimation?.start()
                }
                TtsState.SPEAKING -> {
                    binding.content.ttsSpeechButton.setImageResource(R.drawable.icon_sound_speaking)
                    binding.content.ttsSpeechButton.isEnabled = false
                    ttsButtonAnimation?.cancel()
                    ttsButtonAnimation = binding.content.ttsSpeechButton.tappableEndAnimation()
                    ttsButtonAnimation?.start()
                }
                else -> {
                    binding.content.ttsSpeechButton.isEnabled = false
                    ttsButtonAnimation?.cancel()
                    ttsButtonAnimation = binding.content.ttsSpeechButton.tappableEndAnimation()
                    ttsButtonAnimation?.start()
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)

        val adStateObserver = Observer<AdState> { state ->
            when(state){
                AdState.HIDE -> {}
                AdState.SHOW -> {
                    viewModel.showInterstitialAd(this)
                    viewModel.adState.postValue(AdState.HIDE)
                }
                else -> {}
            }
        }
        viewModel.adState.observe(this, adStateObserver)

        val addToFavouritesButtonStateObserver = Observer<AddToFavouritesState> { state ->
            when(state){
                AddToFavouritesState.VISIBLE_SONG_NOT_IN_FAVOURITES -> {
                    binding.content.addToFavouritesButton.let {
                        it.setOnClickListener {
                            viewModel.addCurrentTrackToFavourites()
                        }

                        it.setImageResource(R.drawable.icon_favourite)
                        it.appear(R.anim.slide_in_top)
                    }
                }
                AddToFavouritesState.VISIBLE_SONG_IN_FAVOURITES -> {
                    binding.content.addToFavouritesButton.let {
                        it.setOnClickListener {
                            viewModel.removeCurrentTrackFromFavourites()
                        }

                        it.setImageResource(R.drawable.icon_favourite_active)
                        it.appear(R.anim.slide_in_top)
                    }
                }
                else -> {
                    binding.content.addToFavouritesButton.let {
                        it.setOnClickListener {}
                        it.disappear(R.anim.slide_out_top)
                    }
                }
            }
        }
        viewModel.addToFavouritesState.observe(this, addToFavouritesButtonStateObserver)

        val notificationObserver = Observer<QuizNotification> { state ->
            if(state != QuizNotification.LOADING){
                binding.content.loadIndicatorProgressBar.visibility = View.GONE
            }

            when(state){
                QuizNotification.LOADING -> {
                    binding.content.loadIndicatorProgressBar.visibility = View.VISIBLE
                }
                QuizNotification.READY_TO_START -> {
                    viewModel.info.value = getString(R.string.ready_to_start)
                }
                QuizNotification.ERROR_PLAYLIST_LOAD -> {
                    viewModel.info.value = getString(R.string.error_playlist_load_description)
                    showInfo(QuizNotification.ERROR_PLAYLIST_LOAD)
                    viewModel.notification.postValue(QuizNotification.EMPTY)
                }
                QuizNotification.ERROR_PLAY_SONG -> {
                    viewModel.info.value = getString(R.string.error_play_song_description)
                    showInfo(QuizNotification.ERROR_PLAY_SONG)
                    viewModel.notification.postValue(QuizNotification.PLAY)
                }
                QuizNotification.ERROR_SPEAK_TO_USER -> {
                    viewModel.info.value = getString(R.string.error_speak_to_user_description)
                    showInfo(QuizNotification.ERROR_SPEAK_TO_USER)
                    viewModel.notification.postValue(QuizNotification.PLAY)
                }
                QuizNotification.ADDED_TO_FAVOURITES -> {
                    showInfo(QuizNotification.ADDED_TO_FAVOURITES)
                    viewModel.notification.postValue(QuizNotification.PLAY)
                }
                QuizNotification.REMOVED_FROM_FAVOURITES -> {
                    showInfo(QuizNotification.REMOVED_FROM_FAVOURITES)
                    viewModel.notification.postValue(QuizNotification.PLAY)
                }
                QuizNotification.EXIT -> {
                    navigateToMainMenu()
                }
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)

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
                view.disappear(R.anim.slide_out_left)
            }
            else if(info.isNotBlank()){
                view.appear(R.anim.slide_in_left, true)
            }

            binding.content.tvInfo.text = info
            // scroll to the bottom of the scrollview
            view.scrollTo(0, view.bottom)
        }
        viewModel.info.observe(this, infoObserver)

        val recognitionObserver = Observer<String> { recognition ->
            val view = binding.content.svRecognition

            if(recognition.isBlank() && binding.content.tvRecognition.text.isNotBlank()){
                view.disappear(R.anim.slide_out_right)
            }
            else if(recognition.isNotBlank() && binding.content.tvRecognition.text.isBlank()){
                view.appear(R.anim.slide_in_right, true)
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

        // to set a placeholder which defines the height
        binding.content.standing.itemPlaceholder.tvName.text = ""
        binding.content.standing.itemPlaceholder.tvScore.text = ""
        binding.content.standing.itemPlaceholder.standingItemLayout.visibility = View.INVISIBLE
        // list of real items
        val playerStandingsToSet = listOf(binding.content.standing.item1, binding.content.standing.item2, binding.content.standing.item3, binding.content.standing.item4)
        val quizStandingObserver = Observer<ViewModelQuizState> { state ->

            val roundText = getString(R.string.current_per_round, state.currentRound.toString(), state.numRounds.toString())
            if(roundText != binding.content.standing.round.text){
                binding.content.standing.round.text = roundText
                binding.content.standing.round.changedAnimation().start()
                binding.content.standing.roundPlaceholder.text = roundText
            }

            if(!state.isFinished && state.currentRound != 0 && state.numRounds != 0){
                // if currently invisible, animate to appear
                binding.content.standing.round.appear(R.anim.slide_in_top)
            }
            else{
                // if currently visible, animate to hide
                binding.content.standing.round.disappear(R.anim.slide_out_top)
            }

            // player points
            playerStandingsToSet.forEachIndexed { index, item ->
                if(state.players.size > index){
                    // if item is not visible, show it
                    item.standingItemLayout.appear(R.anim.slide_in_top)

                    val currentPlayerData = state.players[index]
                    val scoreTextToSet = currentPlayerData.points.toString()
                    // if the score text has changed, set and animate it
                    if(scoreTextToSet != item.tvScore.text){
                        item.tvScore.text = scoreTextToSet
                        item.tvScore.changedAnimation().start()
                    }
                    // if the name text has changed, set and animate it
                    if(currentPlayerData.name != item.tvName.text){
                        item.tvName.text = currentPlayerData.name
                        item.tvName.changedAnimation().start()
                    }

                    // if this is the current player and the quiz is not finished
                    if(state.currentPlayerIdx == index && !state.isFinished && state.numRounds > 0){
                        val color = getColor(R.color.colorActive)
                        item.tvScore.setTextColor(color)
                        item.tvName.setTextColor(color)
                        item.tvName.textColors
                    }
                    else{
                        val color = getColor(R.color.colorIcon)
                        item.tvScore.setTextColor(color)
                        item.tvName.setTextColor(color)
                    }
                }
                else{
                    // if item is not gone, hide it
                    if(item.standingItemLayout.visibility != View.GONE){
                        AnimationUtils.loadAnimation(this, R.anim.slide_out_top).also {
                            item.standingItemLayout.startAnimation(it)
                            item.standingItemLayout.visibility = View.GONE
                        }
                    }
                }
            }

        }
        viewModel.viewModelQuizStanding.observe(this, quizStandingObserver)

        val guessesToSet = listOf(binding.content.feedback.guess1, binding.content.feedback.guess2)
        val currentGuessObserver = Observer<List<ViewModelGuessItem>> { guesses ->
            // last guesses
            guessesToSet.forEachIndexed { index, item ->
                if(guesses.size > index){
                    val currentGuess = guesses[index]
                    item.text = currentGuess.truth

                    val drawable = when(currentGuess.isAccepted){
                        true -> {
                            ContextCompat.getDrawable(applicationContext, R.drawable.icon_correct)
                        }
                        false -> {
                            ContextCompat.getDrawable(applicationContext, R.drawable.icon_incorrect)
                        }
                    }
                    item.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                    // if item is not visible, show it
                    item.appear(R.anim.slide_in_bottom)
                }
                else{
                    // if item is not invisible, hide it
                    item.disappear(R.anim.slide_out_bottom)
                }
            }
        }
        viewModel.currentGuesses.observe(this, currentGuessObserver)

        val endFeedbackObserver = Observer<ViewModelEndFeedback> { feedback ->
            // if valid end feedback observed
            if(feedback.numWinners >= 0){
                if(feedback.numWinners == 0){
                    binding.content.feedback.endIndicator.text = getString(R.string.c_next_time)
                    val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.icon_luck)
                    binding.content.feedback.endIndicator.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                }
                else{
                    binding.content.feedback.endIndicator.text = feedback.winnerNames
                    val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.icon_celebration)
                    binding.content.feedback.endIndicator.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                }

                // if item is not visible, show it
                binding.content.feedback.endIndicator.appear(R.anim.slide_in_bottom)
            }
            else{
                // if item is not invisible, hide it
                binding.content.feedback.endIndicator.disappear(R.anim.slide_out_bottom)
            }
        }
        viewModel.endFeedback.observe(this, endFeedbackObserver)

    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    override fun boardingCheck(){
        val keyBoardingFlag = getString(R.string.PREF_KEY_BOARDING_QUIZ_SHOWED)
        // get saved info from preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val boardingFlag = sharedPreferences.getBoolean(keyBoardingFlag, false)

        if(!boardingFlag){
            MaterialTapTargetSequence().addPrompt(
                MaterialTapTargetPrompt.Builder(this)
                .setTarget(binding.content.userSpeechButton)
                .setPrimaryText(getString(R.string.boarding_quiz_user_input))
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setBackgroundColour(getColor(R.color.colorOnboardingBackground))
                .setFocalColour(getColor(R.color.colorOnboardingFocal))
                .create()
            ).addPrompt(
                MaterialTapTargetPrompt.Builder(this)
                .setTarget(binding.content.ttsSpeechButton)
                .setPrimaryText("${getString(R.string.boarding_quiz_speech)} ${getString(R.string.boarding_tap_to_start)}")
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setBackgroundColour(getColor(R.color.colorOnboardingBackground))
                .setFocalColour(getColor(R.color.colorOnboardingFocal))
                    .setPromptStateChangeListener { prompt, state ->
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                            // persist showed flag to preferences
                            with(sharedPreferences.edit()){
                                remove(keyBoardingFlag)
                                putBoolean(keyBoardingFlag, true)
                                apply()
                            }
                        }
                    }
                .create()
            ).show()
        }
    }

    private fun showInfo(infoType: QuizNotification){
        when(infoType){
            QuizNotification.ERROR_PLAYLIST_LOAD -> {
                val message = getString(R.string.error_listable_load)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
            QuizNotification.ERROR_PLAY_SONG -> {
                val message = getString(R.string.error_play_song)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
            QuizNotification.ERROR_SPEAK_TO_USER -> {
                val message = getString(R.string.error_speak_to_user)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
            QuizNotification.ADDED_TO_FAVOURITES -> {
                val message = getString(R.string.added_to_favourites)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
            QuizNotification.REMOVED_FROM_FAVOURITES -> {
                val message = getString(R.string.removed_from_favourites)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    private fun playlistColorSetter(colorInt: Int){
        val backgroundColor = getColor(R.color.colorBackground)
        val playlistColor = colorInt

        //val darkness = 1-(0.299*Color.red(colorInt) + 0.587*Color.green(colorInt) + 0.114*Color.blue(colorInt))/255

        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(playlistColor, backgroundColor))
        gradientDrawable.cornerRadius = 0F
        binding.root.background = gradientDrawable
        this.window.statusBarColor = playlistColor
    }

    private fun showTypeInputDialog() {
        val inputDialog = AppDialogInput(this, getString(R.string.type_input), getString(R.string.type_input_description))
        inputDialog.setPositiveButton {
            viewModel.explicitUserInput(it)
        }
        inputDialog.show()
    }

}