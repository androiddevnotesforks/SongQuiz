package com.aaronfodor.android.songquiz.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentInfoTrackBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.getDifficulty
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar

class InfoTrackFragment : AppFragment(R.layout.fragment_info_track) {

    private val binding: FragmentInfoTrackBinding by viewBinding()

    private lateinit var viewModel: InfoTrackViewModel

    var imageSize = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[InfoTrackViewModel::class.java]

        imageSize = resources.getDimension(R.dimen.game_image_pixels).toInt()

        val navController = NavHostFragment.findNavController(this)
        when (navController.currentDestination?.id) {
            R.id.nav_info_from_favourites -> {
                viewModel.infoScreenCaller = InfoTrackScreenCaller.FAVOURITES
            }
            else -> {
                viewModel.infoScreenCaller = InfoTrackScreenCaller.UNSPECIFIED
            }
        }

        val safeArgs: InfoTrackFragmentArgs by navArgs()
        viewModel.setItemById(safeArgs.trackId, forceLoad = false)
    }

    override fun subscribeViewModel() {

        val uiStateObserver = Observer<InfoTrackUiState> { state ->
            if(state == InfoTrackUiState.LOADING){
                binding.loadIndicatorProgressBar.visibility = View.VISIBLE
            }
            else{
                binding.loadIndicatorProgressBar.visibility = View.GONE
            }

            when (state) {
                InfoTrackUiState.CLOSE -> {
                    closeScreen()
                }
                InfoTrackUiState.READY -> {}
                else -> {}
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val playlistObserver = Observer<ViewModelPlaylist> { item ->

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

            binding.content.content2.text = item.description
            if(item.description.isNotBlank()){
                binding.content.content2.visibility = View.VISIBLE
            }
            else{
                binding.content.content2.visibility = View.GONE
            }

            binding.content.content3.text = getString(R.string.songs_followers_difficulty, item.tracks.size.toString(), item.followers.toString(), item.getDifficulty().toString())
            binding.content.content3.visibility = View.VISIBLE


            when (viewModel.infoScreenCaller) {

                InfoTrackScreenCaller.FAVOURITES -> {
                    val listen = {}
                    val playContentDescription = getString(R.string.listen)
                    binding.content.fabPrimaryAction.visibility = View.VISIBLE
                    binding.content.tvPrimaryAction.visibility = View.VISIBLE
                    binding.content.fabPrimaryAction.setImageResource(R.drawable.icon_play)
                    binding.content.fabPrimaryAction.contentDescription = playContentDescription
                    binding.content.tvPrimaryAction.text = playContentDescription
                    binding.content.fabPrimaryAction.setOnClickListener { listen() }
                    binding.content.tvPrimaryAction.setOnClickListener { listen() }

                    val viewOnSpotify = {
                        val spotifyPageUri = Uri.parse(getString(R.string.spotify_open_playlist, item.id))
                        val spotifyPageIntent = Intent(Intent.ACTION_VIEW, spotifyPageUri)
                        startActivity(spotifyPageIntent)
                    }
                    val viewSpotifyContentDescription = getString(R.string.view_on_spotify)
                    binding.content.fabSecondaryAction.visibility = View.VISIBLE
                    binding.content.tvSecondaryAction.visibility = View.VISIBLE
                    binding.content.fabSecondaryAction.setImageResource(R.drawable.icon_spotify)
                    binding.content.fabSecondaryAction.contentDescription = viewSpotifyContentDescription
                    binding.content.tvSecondaryAction.text = viewSpotifyContentDescription
                    binding.content.fabSecondaryAction.setOnClickListener { viewOnSpotify() }
                    binding.content.tvSecondaryAction.setOnClickListener { viewOnSpotify() }

                    val deleteTrack = {
                        deleteItem(item.name)
                    }
                    val contentDescription = getString(R.string.content_description_delete)
                    binding.content.fabTertiaryAction.visibility = View.VISIBLE
                    binding.content.tvTertiaryAction.visibility = View.VISIBLE
                    binding.content.fabTertiaryAction.setImageResource(R.drawable.icon_delete)
                    binding.content.fabTertiaryAction.contentDescription = contentDescription
                    binding.content.tvTertiaryAction.text = contentDescription
                    binding.content.fabTertiaryAction.setOnClickListener { deleteTrack() }
                    binding.content.tvTertiaryAction.setOnClickListener { deleteTrack() }
                }

                else -> {
                    binding.content.fabPrimaryAction.visibility = View.INVISIBLE
                    binding.content.tvPrimaryAction.visibility = View.INVISIBLE
                    binding.content.fabPrimaryAction.setOnClickListener {}
                    binding.content.tvPrimaryAction.setOnClickListener {}

                    binding.content.fabSecondaryAction.visibility = View.INVISIBLE
                    binding.content.tvSecondaryAction.visibility = View.INVISIBLE
                    binding.content.fabSecondaryAction.setOnClickListener {}
                    binding.content.tvSecondaryAction.setOnClickListener {}

                    binding.content.fabTertiaryAction.visibility = View.INVISIBLE
                    binding.content.tvTertiaryAction.visibility = View.INVISIBLE
                    binding.content.fabTertiaryAction.setOnClickListener {}
                    binding.content.tvTertiaryAction.setOnClickListener {}
                }

            }

        }
        viewModel.item.observe(this, playlistObserver)

        viewModel.subscribeTtsListeners()
        val ttsStateObserver = Observer<TtsInfoTrackState> { state ->
            when(state){
                TtsInfoTrackState.ENABLED -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_on)
                    binding.fabSpeak.setOnClickListener {
                        val item = viewModel.item.value
                        item?.let {
                            var text = item.name + ". " + getString(R.string.owner_text, item.owner) + ". " + item.description + ". "
                            text = text.replace("\n\n", ".\n\n").replace("..", ".").replace(" . ", " ")
                            viewModel.speak(text)
                        }
                    }
                }
                TtsInfoTrackState.SPEAKING -> {
                    binding.fabSpeak.setImageResource(R.drawable.icon_sound_off)
                    binding.fabSpeak.setOnClickListener {
                        viewModel.stopSpeaking()
                    }
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)

        val notificationObserver = Observer<InfoTrackUiNotification> { notification ->
            when(notification){
                InfoTrackUiNotification.ERROR_LOAD -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_load), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoTrackUiNotification.NONE)
                }
                InfoTrackUiNotification.ERROR_DELETE_ITEM -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoTrackUiNotification.NONE)
                }
                InfoTrackUiNotification.SUCCESS_DELETE_ITEM -> {
                    // don't do anything, exit and the next fragment will show the notification
                }
                InfoTrackUiNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)
    }

    private fun deleteItem(name: String) {
        val dialog = AppDialog(this.requireContext(), getString(R.string.delete_track),
            getString(R.string.delete_track_description, name), R.drawable.icon_delete)
        dialog.setPositiveButton {
            viewModel.deleteItem()
        }
        dialog.show()
    }

    private fun closeScreen(){
        val navHostFragment = NavHostFragment.findNavController(this)

        when(viewModel.infoScreenCaller){

            InfoTrackScreenCaller.FAVOURITES -> {
                if(viewModel.notification.value == InfoTrackUiNotification.SUCCESS_DELETE_ITEM){
                    FavouritesViewModel.notificationFromCaller = FavouritesNotification.SUCCESS_DELETE_TRACK
                }
                navHostFragment.navigate(R.id.to_nav_favourites, null)
            }

            InfoTrackScreenCaller.UNSPECIFIED -> {
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

    override fun unsubscribeViewModel() {
        viewModel.unsubscribeTtsListeners()
    }

}