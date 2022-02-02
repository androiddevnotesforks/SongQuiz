package com.aaronfodor.android.songquiz.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentInfoBinding
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence

class InfoFragment : AppFragment(R.layout.fragment_info), AuthRequestModule {

    private val binding: FragmentInfoBinding by viewBinding()

    private lateinit var viewModel: InfoViewModel

    var imageSize = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(InfoViewModel::class.java)

        imageSize = resources.getDimension(R.dimen.game_image_pixels).toInt()

        val navController = NavHostFragment.findNavController(this)
        when (navController.currentDestination?.id) {
            R.id.nav_info_from_home -> {
                viewModel.infoScreenCaller = InfoScreenCaller.HOME
            }
            R.id.nav_info_from_play -> {
                viewModel.infoScreenCaller = InfoScreenCaller.PLAY
            }
            R.id.nav_info_from_add_playlists -> {
                viewModel.infoScreenCaller = InfoScreenCaller.ADD_PLAYLIST
            }
            else -> {
                viewModel.infoScreenCaller = InfoScreenCaller.UNSPECIFIED
            }
        }

        val safeArgs: InfoFragmentArgs by navArgs()
        viewModel.setPlaylistById(safeArgs.playlistId)
    }

    override fun subscribeViewModel() {
        val uiStateObserver = Observer<InfoUiState> { state ->
            if(state == InfoUiState.LOADING){
                binding.loadIndicatorProgressBar.visibility = View.VISIBLE
            }
            else{
                binding.loadIndicatorProgressBar.visibility = View.GONE
            }

            when (state) {
                InfoUiState.CLOSE -> {
                    closeScreen()
                }
                else -> {
                    showInfo(state)
                }
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val playlistObserver = Observer<Playlist> { playlist ->

            if(playlist.previewImageUri.isEmpty()){
                binding.ivLogo.setImageResource(R.drawable.icon_album)
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
                    .load(playlist.previewImageUri)
                    .transition(DrawableTransitionOptions.with(DrawableCrossFadeFactory()))
                    .apply(options)
                    .into(binding.ivLogo)
            }

            binding.content.title.text = playlist.name
            binding.content.owner.text = getString(R.string.owner_text, playlist.owner)
            binding.content.description.text = playlist.description

            if(viewModel.uiState.value == InfoUiState.READY){
                binding.content.numSongsFollowers.text = getString(R.string.num_songs_followers, playlist.tracks.size.toString(), playlist.followers.toString())
            }
            else{
                binding.content.numSongsFollowers.text = ""
            }

            val play = {
                viewModel.startQuiz()
            }
            val playContentDescription = getString(R.string.play)
            binding.content.fabPrimaryAction.setImageResource(R.drawable.icon_play)
            binding.content.fabPrimaryAction.contentDescription = playContentDescription
            binding.content.tvPrimaryAction.text = playContentDescription
            binding.content.fabPrimaryAction.setOnClickListener { play() }
            binding.content.tvPrimaryAction.setOnClickListener { play() }

            val viewOnSpotify = {
                val spotifyPageUri = Uri.parse(getString(R.string.spotify_open_playlist, playlist.id))
                val spotifyPageIntent = Intent(Intent.ACTION_VIEW, spotifyPageUri)
                startActivity(spotifyPageIntent)
            }
            val viewSpotifyContentDescription = getString(R.string.view_on_spotify)
            binding.content.fabSecondaryAction.setImageResource(R.drawable.icon_spotify)
            binding.content.fabSecondaryAction.contentDescription = viewSpotifyContentDescription
            binding.content.tvSecondaryAction.text = viewSpotifyContentDescription
            binding.content.fabSecondaryAction.setOnClickListener { viewOnSpotify() }
            binding.content.tvSecondaryAction.setOnClickListener { viewOnSpotify() }

            when (viewModel.infoScreenCaller) {
                InfoScreenCaller.HOME -> {
                    binding.content.fabTertiaryAction.visibility = View.INVISIBLE
                    binding.content.tvTertiaryAction.visibility = View.INVISIBLE
                    binding.content.fabTertiaryAction.setOnClickListener {}
                    binding.content.tvTertiaryAction.setOnClickListener {}
                }
                InfoScreenCaller.PLAY -> {
                    val deletePlaylist = {
                        deletePlaylist(playlist.name)
                    }
                    val contentDescription = getString(R.string.content_description_delete)
                    binding.content.fabTertiaryAction.setImageResource(R.drawable.icon_delete)
                    binding.content.fabTertiaryAction.contentDescription = contentDescription
                    binding.content.tvTertiaryAction.text = contentDescription
                    binding.content.fabTertiaryAction.visibility = View.VISIBLE
                    binding.content.tvTertiaryAction.visibility = View.VISIBLE
                    binding.content.fabTertiaryAction.setOnClickListener { deletePlaylist() }
                    binding.content.tvTertiaryAction.setOnClickListener { deletePlaylist() }
                }
                InfoScreenCaller.ADD_PLAYLIST -> {
                    val addPlaylist = {
                        viewModel.addPlaylist()
                    }
                    val contentDescription = getString(R.string.content_description_add)
                    binding.content.fabTertiaryAction.setImageResource(R.drawable.icon_add)
                    binding.content.fabTertiaryAction.contentDescription = contentDescription
                    binding.content.tvTertiaryAction.text = contentDescription
                    binding.content.fabTertiaryAction.visibility = View.VISIBLE
                    binding.content.tvTertiaryAction.visibility = View.VISIBLE
                    binding.content.fabTertiaryAction.setOnClickListener { addPlaylist() }
                    binding.content.tvTertiaryAction.setOnClickListener { addPlaylist() }
                }
                else -> {
                    binding.content.fabTertiaryAction.visibility = View.INVISIBLE
                    binding.content.tvTertiaryAction.visibility = View.INVISIBLE
                    binding.content.fabTertiaryAction.setOnClickListener {}
                    binding.content.tvTertiaryAction.setOnClickListener {}
                }
            }

        }
        viewModel.playlist.observe(this, playlistObserver)

        viewModel.subscribeTtsListeners()
        val ttsStateObserver = Observer<TtsInfoState> { state ->
            when(state){
                TtsInfoState.ENABLED -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_on)
                    binding.fabSpeak.setOnClickListener {
                        val playlist = viewModel.playlist.value
                        playlist?.let {

                            val textSongsFollowers = if(viewModel.uiState.value == InfoUiState.READY){
                                getString(R.string.num_songs_followers, playlist.tracks.size.toString(), playlist.followers.toString())
                            }
                            else{
                                ""
                            }

                            var text = playlist.name + ". " + getString(R.string.owner_text, playlist.owner) + ". " +
                                    playlist.description + ". " + textSongsFollowers
                            text = text.replace("\n\n", ".\n\n").replace("..", ".").replace(" . ", " ")
                            viewModel.speak(text)
                        }
                    }
                }
                TtsInfoState.SPEAKING -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_off)
                    binding.fabSpeak.setOnClickListener {
                        viewModel.stopSpeaking()
                    }
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)
    }

    private fun deletePlaylist(name: String) {
        val dialog = AppDialog(this.requireContext(), getString(R.string.delete_playlist),
            getString(R.string.delete_playlist_description, name), R.drawable.icon_delete)
        dialog.setPositiveButton {
            viewModel.deletePlaylist()
        }
        dialog.show()
    }

    private fun closeScreen(){
        val navHostFragment = NavHostFragment.findNavController(this)
        when(viewModel.infoScreenCaller){
            InfoScreenCaller.HOME -> {
                navHostFragment.navigate(R.id.to_nav_home, null)
            }
            InfoScreenCaller.PLAY -> {
                navHostFragment.navigate(R.id.to_nav_play, null)
            }
            InfoScreenCaller.ADD_PLAYLIST -> {
                navHostFragment.navigate(R.id.to_nav_add, null)
            }
            InfoScreenCaller.UNSPECIFIED -> {
                navHostFragment.navigate(R.id.to_nav_home, null)
            }
        }
        viewModel.ready()
    }

    override fun appearingAnimations() {
        val rightAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
        binding.fabSpeak.startAnimation(rightAnimation)
        binding.fabSpeak.visibility = View.VISIBLE

        val leftAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
        binding.content.fabPrimaryAction.startAnimation(leftAnimation)
        binding.content.fabSecondaryAction.startAnimation(leftAnimation)
        binding.content.fabTertiaryAction.startAnimation(leftAnimation)
    }

    override fun onboardingDialog(){
        val keyOnboardingFlag = getString(R.string.PREF_KEY_ONBOARDING_INFO_SHOWED)
        // get saved info from preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val onboardingFlag = sharedPreferences.getBoolean(keyOnboardingFlag, false)

        if(!onboardingFlag){
            MaterialTapTargetSequence().addPrompt(
                MaterialTapTargetPrompt.Builder(this)
                .setTarget(binding.fabSpeak)
                .setPrimaryText(getString(R.string.onboarding_info_listen))
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setBackgroundColour(ContextCompat.getColor(requireContext(), R.color.colorOnboardingBackground))
                .setFocalColour(ContextCompat.getColor(requireContext(), R.color.colorOnboardingFocal))
                .create()
        ).addPrompt(
            MaterialTapTargetPrompt.Builder(this)
                .setTarget(binding.content.fabPrimaryAction)
                .setPrimaryText(getString(R.string.onboarding_info_start_quiz))
                .setAnimationInterpolator(FastOutSlowInInterpolator())
                .setBackgroundColour(ContextCompat.getColor(requireContext(), R.color.colorOnboardingBackground))
                .setFocalColour(ContextCompat.getColor(requireContext(), R.color.colorOnboardingFocal))
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

    override fun unsubscribeViewModel() {
        viewModel.unsubscribeTtsListeners()
    }

    private fun showInfo(infoType: InfoUiState){
        when(infoType){
            InfoUiState.ERROR_PLAYLIST_LOAD -> {
                val message = getString(R.string.error_playlist_load)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.ready()
            }
            InfoUiState.AUTH_NEEDED -> {
                startAuthentication()
            }
            InfoUiState.START_QUIZ -> {
                showQuizScreen()
            }
            else -> {}
        }
    }

    private fun showQuizScreen(){
        viewModel.ready()
        viewModel.playlist.value?.let {
            // Log start game event
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.LEVEL_START){
                param(FirebaseAnalytics.Param.ITEM_ID, it.id)
            }

            val intent = Intent(this.requireContext(), QuizActivity::class.java)
            intent.putExtra(QuizActivity.PLAYLIST_KEY, it.id)
            startActivity(intent)
        }
    }

    override var authLauncherStarted = false
    override val authLauncher = registerForActivityResult(AuthRequestContract()){ isAuthSuccess ->
        if(isAuthSuccess){
            viewModel.startQuiz()
        }
        else{
            viewModel.ready()
        }
        authLauncherStarted = false
    }

}