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
import com.aaronfodor.android.songquiz.databinding.FragmentInfoTrackBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelTrack
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toPrettyString
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

        val trackObserver = Observer<ViewModelTrack> { item ->

            if(item.imageUri.isEmpty()){
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
                    .load(item.imageUri)
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

            getString(R.string.artist_text, item.artists.toPrettyString())

            binding.content.content.text = detailsText(item, true)
            if(binding.content.content.text.isNotBlank()){
                binding.content.content.visibility = View.VISIBLE
            }
            else{
                binding.content.content.visibility = View.GONE
            }

            when (viewModel.infoScreenCaller) {
                InfoTrackScreenCaller.FAVOURITES -> {

                    val viewOnSpotify = {
                        val spotifyPageUri = Uri.parse(getString(R.string.spotify_open_track, item.id))
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
        viewModel.item.observe(this, trackObserver)

        viewModel.subscribeTtsListeners()
        val ttsStateObserver = Observer<TtsInfoTrackState> { state ->
            when(state){
                TtsInfoTrackState.ENABLED -> {
                    val readLambda = {
                        val item = viewModel.item.value
                        item?.let {
                            var text = item.name + ". " + detailsText(item, false)
                            text = text.replace("\n\n", ".\n\n").replace("..", ".").replace(" . ", " ")
                            viewModel.speak(text)
                        }
                    }
                    val readContentDescription = getString(R.string.read_aloud)

                    binding.content.action1.tv.text = readContentDescription
                    binding.content.action1.tv.setOnClickListener { readLambda() }

                    binding.content.action1.fab.setImageResource(R.drawable.icon_sound_on)
                    binding.content.action1.fab.contentDescription = readContentDescription
                    binding.content.action1.fab.setOnClickListener { readLambda() }
                }
                TtsInfoTrackState.SPEAKING -> {
                    val stopContentDescription = getString(R.string.stop)

                    binding.content.action1.tv.text = stopContentDescription
                    binding.content.action1.tv.setOnClickListener { viewModel.stopSpeaking() }

                    binding.content.action1.tv.text = stopContentDescription
                    binding.content.action1.fab.setImageResource(R.drawable.icon_sound_off)
                    binding.content.action1.fab.setOnClickListener { viewModel.stopSpeaking() }
                }
            }
        }
        viewModel.ttsState.observe(this, ttsStateObserver)

        viewModel.subscribeMediaPlayerListeners()
        val mediaPlayerStateObserver = Observer<MediaPlayerInfoTrackState> { state ->
            when(state){
                MediaPlayerInfoTrackState.PLAYING -> {
                    mediaPlayerPlayState()
                }
                MediaPlayerInfoTrackState.STOPPED -> {
                    mediaPlayerStoppedState()
                }
                else -> {}
            }
        }
        viewModel.mediaPlayerState.observe(this, mediaPlayerStateObserver)

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
                InfoTrackUiNotification.ERROR_PLAY_SONG -> {
                    Snackbar.make(binding.root, getString(R.string.error_play_song_description), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(InfoTrackUiNotification.NONE)
                }
                InfoTrackUiNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)
    }

    private fun mediaPlayerPlayState(){
        val stopContentDescription = getString(R.string.stop)
        binding.fabMain.setImageResource(R.drawable.icon_stop)
        binding.fabMain.contentDescription = stopContentDescription
        binding.fabMain.setOnClickListener { viewModel.stopSong() }
    }

    private fun mediaPlayerStoppedState(){
        val listenContentDescription = getString(R.string.listen)
        binding.fabMain.setImageResource(R.drawable.icon_play)
        binding.fabMain.contentDescription = listenContentDescription
        binding.fabMain.setOnClickListener { viewModel.playSong() }
    }

    private fun detailsText(item: ViewModelTrack, createForDisplay: Boolean = false) : String {
        val sentenceSeparator = if(createForDisplay){
            "\n\n"
        }
        else{
            ". "
        }

        // millis -> sec
        val durationSec = item.durationMs/1000
        val minutes = durationSec / 60
        val seconds = durationSec % 60

        return getString(R.string.artist_text, item.artists.toPrettyString()) + sentenceSeparator +
            getString(R.string.track_info, item.album, minutes.toString(), seconds.toString(), item.popularity.toString())
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
        binding.fabMain.appear(R.anim.slide_in_right, true)
        binding.content.action1.fab.appear(R.anim.slide_in_left, true)
        binding.content.action2.fab.appear(R.anim.slide_in_left, true)
        binding.content.action3.fab.appear(R.anim.slide_in_left, true)
    }

    override fun unsubscribeViewModel() {
        viewModel.unsubscribeTtsListeners()
        viewModel.unsubscribeMediaPlayerListeners()
    }

}