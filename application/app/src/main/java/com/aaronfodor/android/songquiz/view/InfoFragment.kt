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
import com.aaronfodor.android.songquiz.databinding.FragmentInfoBinding
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.view.utils.AppDialog
import com.aaronfodor.android.songquiz.view.utils.AppFragment
import com.aaronfodor.android.songquiz.view.utils.DrawableCrossFadeFactory
import com.aaronfodor.android.songquiz.viewmodel.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar

class InfoFragment : AppFragment(R.layout.fragment_info) {

    private val binding: FragmentInfoBinding by viewBinding()

    private lateinit var viewModel: InfoViewModel

    var imageSize = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(InfoViewModel::class.java)

        imageSize = resources.getDimension(R.dimen.game_image_pixels).toInt()

        val navController = NavHostFragment.findNavController(this)
        when (navController.currentDestination?.id) {
            R.id.nav_info_from_playlists -> {
                viewModel.infoScreenCaller = InfoScreenCaller.PLAYLISTS
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
            when (state) {
                InfoUiState.LOADING -> {
                    binding.loadIndicatorProgressBar.visibility = View.VISIBLE
                }
                InfoUiState.ERROR_PLAYLIST_LOAD -> {
                    binding.loadIndicatorProgressBar.visibility = View.GONE
                    showInfo(InfoUiState.ERROR_PLAYLIST_LOAD)
                }
                InfoUiState.CLOSE -> {
                    closeScreen()
                }
                else -> {
                    binding.loadIndicatorProgressBar.visibility = View.GONE
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
                binding.content.numSongsFollowers.text = getString(R.string.num_songs_followers, playlist.tracks.size, playlist.followers)
            }
            else{
                binding.content.numSongsFollowers.text = ""
            }

            val viewOnSpotify = {
                val spotifyPageUri = Uri.parse(getString(R.string.spotify_open_playlist, playlist.id))
                val spotifyPageIntent = Intent(Intent.ACTION_VIEW, spotifyPageUri)
                startActivity(spotifyPageIntent)
            }
            val contentDescription = getString(R.string.view_on_spotify)
            binding.content.fabPrimaryAction.setImageResource(R.drawable.icon_spotify)
            binding.content.fabPrimaryAction.contentDescription = contentDescription
            binding.content.tvPrimaryAction.text = contentDescription
            binding.content.fabPrimaryAction.setOnClickListener { viewOnSpotify() }
            binding.content.tvPrimaryAction.setOnClickListener { viewOnSpotify() }

            when (viewModel.infoScreenCaller) {
                InfoScreenCaller.PLAYLISTS -> {
                    val deletePlaylist = {
                        deletePlaylist(playlist.name)
                    }
                    val contentDescription = getString(R.string.content_description_delete)
                    binding.content.fabSecondaryAction.setImageResource(R.drawable.icon_delete)
                    binding.content.fabSecondaryAction.contentDescription = contentDescription
                    binding.content.tvSecondaryAction.text = contentDescription
                    binding.content.fabSecondaryAction.visibility = View.VISIBLE
                    binding.content.tvSecondaryAction.visibility = View.VISIBLE
                    binding.content.fabSecondaryAction.setOnClickListener { deletePlaylist() }
                    binding.content.tvSecondaryAction.setOnClickListener { deletePlaylist() }
                }
                InfoScreenCaller.ADD_PLAYLIST -> {
                    val addPlaylist = {
                        viewModel.addPlaylist()
                    }
                    val contentDescription = getString(R.string.content_description_add)
                    binding.content.fabSecondaryAction.setImageResource(R.drawable.icon_add)
                    binding.content.fabSecondaryAction.contentDescription = contentDescription
                    binding.content.tvSecondaryAction.text = contentDescription
                    binding.content.fabSecondaryAction.visibility = View.VISIBLE
                    binding.content.tvSecondaryAction.visibility = View.VISIBLE
                    binding.content.fabSecondaryAction.setOnClickListener { addPlaylist() }
                    binding.content.tvSecondaryAction.setOnClickListener { addPlaylist() }
                }
                else -> {
                    binding.content.fabSecondaryAction.visibility = View.INVISIBLE
                    binding.content.tvSecondaryAction.visibility = View.INVISIBLE
                    binding.content.fabSecondaryAction.setOnClickListener {}
                    binding.content.tvSecondaryAction.setOnClickListener {}
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
                                getString(R.string.num_songs_followers, playlist.tracks.size, playlist.followers)
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
        navHostFragment.navigate(R.id.to_nav_playlists, null)
        viewModel.ready()
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {
        viewModel.unsubscribeTtsListeners()
    }

    private fun showInfo(infoType: InfoUiState){
        when(infoType){
            InfoUiState.ERROR_PLAYLIST_LOAD -> {
                val message = getString(R.string.error_playlist_load)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
            else -> {}
        }
        viewModel.ready()
    }

}