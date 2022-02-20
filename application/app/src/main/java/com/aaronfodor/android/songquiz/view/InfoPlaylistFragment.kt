package com.aaronfodor.android.songquiz.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentInfoPlaylistBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.getDifficulty
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class InfoPlaylistFragment : AppFragment(R.layout.fragment_info_playlist) {

    private val binding: FragmentInfoPlaylistBinding by viewBinding()

    override lateinit var viewModel: InfoPlaylistViewModel

    var imageSize = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[InfoPlaylistViewModel::class.java]

        imageSize = resources.getDimension(R.dimen.game_image_pixels).toInt()

        val safeArgs: InfoPlaylistFragmentArgs by navArgs()
        viewModel.setCaller(safeArgs.callerAsString)
        viewModel.setItem(safeArgs.playlistId, forceLoad = false)
    }

    override fun subscribeViewModel() {

        val uiStateObserver = Observer<InfoPlaylistUiState> { state ->
            if(state == InfoPlaylistUiState.LOADING){
                binding.loadIndicatorProgressBar.visibility = View.VISIBLE
            }
            else{
                binding.loadIndicatorProgressBar.visibility = View.GONE
            }

            when (state) {
                InfoPlaylistUiState.CLOSE -> {
                    closeScreen()
                }
                InfoPlaylistUiState.START_QUIZ -> {
                    showQuizScreen()
                }
                InfoPlaylistUiState.READY_FALLBACK -> {}
                InfoPlaylistUiState.READY_COMPLETE -> {}
                else -> {}
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val itemObserver = Observer<ViewModelPlaylist> { item ->

            if(item.previewImageUri.isEmpty()){
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
                    .load(item.previewImageUri)
                    .transition(DrawableTransitionOptions.with(DrawableCrossFadeFactory()))
                    .apply(options)
                    .into(binding.ivLogo)
            }

            binding.content.title.text = item.name
            if(item.name.isNotBlank()){
                binding.content.title.visibility = View.VISIBLE
            }
            else{
                binding.content.title.visibility = View.GONE
            }

            binding.content.content1.text = getString(R.string.owner_text, item.owner)
            if(item.owner.isNotBlank()){
                binding.content.content1.visibility = View.VISIBLE
            }
            else{
                binding.content.content1.visibility = View.GONE
            }

            binding.content.content2.text = item.description
            if(item.description.isNotBlank()){
                binding.content.content2.visibility = View.VISIBLE
            }
            else{
                binding.content.content2.visibility = View.GONE
            }

            if(viewModel.uiState.value == InfoPlaylistUiState.READY_COMPLETE){
                binding.content.content3.text = getString(R.string.songs_followers_difficulty, item.tracks.size.toString(), item.followers.toString(), item.getDifficulty().toString())
                binding.content.content3.visibility = View.VISIBLE
            }
            else{
                binding.content.content3.text = ""
                binding.content.content3.visibility = View.GONE
            }

            when (viewModel.infoScreenCaller) {

                InfoPlaylistScreenCaller.HOME, InfoPlaylistScreenCaller.PLAY -> {
                    val play = {
                        viewModel.startQuiz()
                    }
                    val playContentDescription = getString(R.string.play)
                    binding.content.action1.fab.visibility = View.VISIBLE
                    binding.content.action1.tv.visibility = View.VISIBLE
                    binding.content.action1.fab.setImageResource(R.drawable.icon_play)
                    binding.content.action1.fab.contentDescription = playContentDescription
                    binding.content.action1.tv.text = playContentDescription
                    binding.content.action1.fab.setOnClickListener { play() }
                    binding.content.action1.tv.setOnClickListener { play() }

                    val viewOnSpotify = {
                        val spotifyPageUri = Uri.parse(getString(R.string.spotify_open_playlist, item.id))
                        val spotifyPageIntent = Intent(Intent.ACTION_VIEW, spotifyPageUri)
                        startActivity(spotifyPageIntent)
                    }
                    val viewSpotifyContentDescription = getString(R.string.view_on_spotify)
                    binding.content.action2.fab.visibility = View.VISIBLE
                    binding.content.action2.tv.visibility = View.VISIBLE
                    binding.content.action2.fab.setImageResource(R.drawable.icon_spotify)
                    binding.content.action2.fab.contentDescription = viewSpotifyContentDescription
                    binding.content.action2.tv.text = viewSpotifyContentDescription
                    binding.content.action2.fab.setOnClickListener { viewOnSpotify() }
                    binding.content.action2.tv.setOnClickListener { viewOnSpotify() }

                    val deletePlaylist = {
                        deleteItem(item.name)
                    }
                    val contentDescription = getString(R.string.content_description_delete)
                    binding.content.action3.fab.visibility = View.VISIBLE
                    binding.content.action3.tv.visibility = View.VISIBLE
                    binding.content.action3.fab.setImageResource(R.drawable.icon_delete)
                    binding.content.action3.fab.contentDescription = contentDescription
                    binding.content.action3.tv.text = contentDescription
                    binding.content.action3.fab.setOnClickListener { deletePlaylist() }
                    binding.content.action3.tv.setOnClickListener { deletePlaylist() }
                }

                InfoPlaylistScreenCaller.ADD_PLAYLIST -> {
                    val addPlaylist = {
                        viewModel.addItem()
                    }
                    val contentDescription = getString(R.string.content_description_add)
                    binding.content.action1.fab.visibility = View.VISIBLE
                    binding.content.action1.tv.visibility = View.VISIBLE
                    binding.content.action1.fab.setImageResource(R.drawable.icon_add)
                    binding.content.action1.fab.contentDescription = contentDescription
                    binding.content.action1.tv.text = contentDescription
                    binding.content.action1.fab.setOnClickListener { addPlaylist() }
                    binding.content.action1.tv.setOnClickListener { addPlaylist() }

                    val play = {
                        viewModel.startQuiz()
                    }
                    val playContentDescription = getString(R.string.play)
                    binding.content.action2.fab.visibility = View.VISIBLE
                    binding.content.action2.tv.visibility = View.VISIBLE
                    binding.content.action2.fab.setImageResource(R.drawable.icon_play)
                    binding.content.action2.fab.contentDescription = playContentDescription
                    binding.content.action2.tv.text = playContentDescription
                    binding.content.action2.fab.setOnClickListener { play() }
                    binding.content.action2.tv.setOnClickListener { play() }

                    val viewOnSpotify = {
                        val spotifyPageUri = Uri.parse(getString(R.string.spotify_open_playlist, item.id))
                        val spotifyPageIntent = Intent(Intent.ACTION_VIEW, spotifyPageUri)
                        startActivity(spotifyPageIntent)
                    }
                    val viewSpotifyContentDescription = getString(R.string.view_on_spotify)
                    binding.content.action3.fab.visibility = View.VISIBLE
                    binding.content.action3.tv.visibility = View.VISIBLE
                    binding.content.action3.fab.setImageResource(R.drawable.icon_spotify)
                    binding.content.action3.fab.contentDescription = viewSpotifyContentDescription
                    binding.content.action3.tv.text = viewSpotifyContentDescription
                    binding.content.action3.fab.setOnClickListener { viewOnSpotify() }
                    binding.content.action3.tv.setOnClickListener { viewOnSpotify() }
                }

                else -> {
                    binding.content.action1.fab.visibility = View.INVISIBLE
                    binding.content.action1.tv.visibility = View.INVISIBLE
                    binding.content.action1.fab.setOnClickListener {}
                    binding.content.action1.tv.setOnClickListener {}

                    binding.content.action2.fab.visibility = View.INVISIBLE
                    binding.content.action2.tv.visibility = View.INVISIBLE
                    binding.content.action2.fab.setOnClickListener {}
                    binding.content.action2.tv.setOnClickListener {}

                    binding.content.action3.fab.visibility = View.INVISIBLE
                    binding.content.action3.tv.visibility = View.INVISIBLE
                    binding.content.action3.fab.setOnClickListener {}
                    binding.content.action3.tv.setOnClickListener {}
                }

            }

        }
        viewModel.item.observe(this, itemObserver)

        viewModel.subscribeTtsListeners()
        val ttsStateObserver = Observer<TtsInfoPlaylistState> { state ->
            when(state){
                TtsInfoPlaylistState.ENABLED -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_on)
                    binding.fabSpeak.setOnClickListener {
                        val item = viewModel.item.value
                        item?.let {

                            val textSongsFollowers = if(viewModel.uiState.value == InfoPlaylistUiState.READY_COMPLETE){
                                getString(R.string.songs_followers_difficulty, item.tracks.size.toString(), item.followers.toString(), item.getDifficulty().toString())
                            }
                            else{
                                ""
                            }

                            var text = item.name + ". " + getString(R.string.owner_text, item.owner) + ". " +
                                    item.description + ". " + textSongsFollowers
                            text = text.replace("\n\n", ".\n\n").replace("..", ".").replace(" . ", " ")
                            viewModel.speak(text)
                        }
                    }
                }
                TtsInfoPlaylistState.SPEAKING -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_off)
                    binding.fabSpeak.setOnClickListener {
                        viewModel.stopSpeaking()
                    }
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)

        val notificationObserver = Observer<InfoPlaylistUiNotification> { notification ->
            when(notification){
                InfoPlaylistUiNotification.FALLBACK_LOAD -> {
                    Snackbar.make(binding.root, getString(R.string.listable_partially_loaded), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoPlaylistUiNotification.NONE)
                }
                InfoPlaylistUiNotification.ERROR_LOAD -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_load), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoPlaylistUiNotification.NONE)
                }
                InfoPlaylistUiNotification.ERROR_ADD_ITEM -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_add), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoPlaylistUiNotification.NONE)
                }
                InfoPlaylistUiNotification.SUCCESS_ADD_ITEM -> {
                    Snackbar.make(binding.root, getString(R.string.success_listable_add), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoPlaylistUiNotification.NONE)
                }
                InfoPlaylistUiNotification.ERROR_DELETE_ITEM -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoPlaylistUiNotification.NONE)
                }
                InfoPlaylistUiNotification.SUCCESS_DELETE_ITEM -> {
                    Snackbar.make(binding.root, getString(R.string.success_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoPlaylistUiNotification.NONE)
                }
                InfoPlaylistUiNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)
    }

    private fun deleteItem(name: String) {
        val dialog = AppDialog(this.requireContext(), getString(R.string.delete_playlist),
            getString(R.string.delete_playlist_description, name), R.drawable.icon_delete)
        dialog.setPositiveButton {
            viewModel.deleteItem()
        }
        dialog.show()
    }

    private fun closeScreen(){
        val navController = NavHostFragment.findNavController(this)
        navController.navigateUp()
        viewModel.ready(InfoPlaylistUiState.READY_FALLBACK)
    }

    override fun appearingAnimations() {
        binding.fabSpeak.appear(R.anim.slide_in_right, true)
        binding.content.action1.fab.appear(R.anim.slide_in_left, true)
        binding.content.action2.fab.appear(R.anim.slide_in_left, true)
        binding.content.action3.fab.appear(R.anim.slide_in_left, true)
    }

    override fun unsubscribeViewModel() {
        viewModel.unsubscribeTtsListeners()
    }

    private fun showQuizScreen(){
        viewModel.ready(InfoPlaylistUiState.READY_FALLBACK)
        viewModel.item.value?.let {
            // Log start game event
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.LEVEL_START){
                param(FirebaseAnalytics.Param.ITEM_ID, it.id)
            }

            val intent = Intent(this.requireContext(), QuizActivity::class.java)
            intent.putExtra(QuizActivity.PLAYLIST_KEY, it.id)
            startActivity(intent)
        }
    }

}