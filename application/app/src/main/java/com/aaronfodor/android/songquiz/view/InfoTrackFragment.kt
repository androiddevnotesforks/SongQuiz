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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar

class InfoTrackFragment : AppFragment(R.layout.fragment_info_track) {

    private val binding: FragmentInfoTrackBinding by viewBinding()

    override lateinit var viewModel: InfoTrackViewModel

    var imageSize = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[InfoTrackViewModel::class.java]

        imageSize = resources.getDimension(R.dimen.game_image_pixels).toInt()

        val safeArgs: InfoTrackFragmentArgs by navArgs()
        viewModel.setCaller(safeArgs.callerAsString)
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

            binding.content.content.text = item.description
            if(item.owner.isNotBlank()){
                binding.content.content.visibility = View.VISIBLE
            }
            else{
                binding.content.content.visibility = View.GONE
            }


            when (viewModel.infoScreenCaller) {

                InfoTrackScreenCaller.FAVOURITES -> {
                    val listen = {}
                    val playContentDescription = getString(R.string.listen)
                    binding.content.action1.fab.visibility = View.VISIBLE
                    binding.content.action1.tv.visibility = View.VISIBLE
                    binding.content.action1.fab.setImageResource(R.drawable.icon_play)
                    binding.content.action1.fab.contentDescription = playContentDescription
                    binding.content.action1.tv.text = playContentDescription
                    binding.content.action1.fab.setOnClickListener { listen() }
                    binding.content.action1.tv.setOnClickListener { listen() }

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

                    val deleteTrack = {
                        deleteItem(item.name)
                    }
                    val contentDescription = getString(R.string.content_description_delete)
                    binding.content.action3.fab.visibility = View.VISIBLE
                    binding.content.action3.tv.visibility = View.VISIBLE
                    binding.content.action3.fab.setImageResource(R.drawable.icon_delete)
                    binding.content.action3.fab.contentDescription = contentDescription
                    binding.content.action3.tv.text = contentDescription
                    binding.content.action3.fab.setOnClickListener { deleteTrack() }
                    binding.content.action3.tv.setOnClickListener { deleteTrack() }
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
                    Snackbar.make(binding.root, getString(R.string.success_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoTrackUiNotification.NONE)
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
        val navController = NavHostFragment.findNavController(this)
        navController.navigateUp()
        viewModel.ready()
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

}