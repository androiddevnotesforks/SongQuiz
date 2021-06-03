package com.arpadfodor.android.songquiz.view

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.FragmentPlaylistAddBinding
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.arpadfodor.android.songquiz.view.utils.AppFragment
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsAddUiState
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsAddViewModel
import com.google.android.material.snackbar.Snackbar

class PlaylistAddFragment : AppFragment(R.layout.fragment_playlist_add) {

    private val binding: FragmentPlaylistAddBinding by viewBinding()

    private lateinit var viewModel: PlaylistsAddViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaylistsAddViewModel::class.java)

        val addLambda: (Playlist) -> Unit = { playlist -> addPlaylist(playlist.id) }

        val playlistsAdapter = PlaylistAddAdapter(this.requireContext(), addLambda)
        binding.RecyclerViewPlaylists.adapter = playlistsAdapter

        binding.fabBack.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_nav_playlist_add_to_nav_playlists, null))
    }

    override fun subscribeViewModel() {
        val playlistsFoundObserver = Observer<List<Playlist>> { playlists ->
            (binding.RecyclerViewPlaylists.adapter as PlaylistAddAdapter).submitList(playlists)
        }
        viewModel.setPlaylistsFromCompanion()
        viewModel.playlistsFound.observe(this, playlistsFoundObserver)

        val playlistsAddStateObserver = Observer<PlaylistsAddUiState> { state ->

            if(state != PlaylistsAddUiState.LOADING){
                binding.loadIndicatorProgressBar.visibility = View.GONE
            }

            when(state){
                PlaylistsAddUiState.LOADING -> {
                    binding.loadIndicatorProgressBar.visibility = View.VISIBLE
                }
                PlaylistsAddUiState.READY -> {}
                PlaylistsAddUiState.ERROR_ADD_PLAYLIST -> {
                    showInfo(PlaylistsAddUiState.ERROR_ADD_PLAYLIST)
                }
                PlaylistsAddUiState.SUCCESS_ADD_PLAYLIST -> {
                    showInfo(PlaylistsAddUiState.SUCCESS_ADD_PLAYLIST)
                }
                PlaylistsAddUiState.PLAYLIST_ALREADY_ADDED -> {
                    showInfo(PlaylistsAddUiState.PLAYLIST_ALREADY_ADDED)
                }
                else -> {}
            }
        }
        viewModel.playlistsAddState.observe(this, playlistsAddStateObserver)
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun addPlaylist(id: String){
        viewModel.addPlaylistById(id)
    }

    private fun showInfo(errorType: PlaylistsAddUiState){
        val message = when(errorType){
            PlaylistsAddUiState.ERROR_ADD_PLAYLIST -> {
                getString(R.string.error_playlist_add)
            }
            PlaylistsAddUiState.SUCCESS_ADD_PLAYLIST -> {
                getString(R.string.success_playlist_add)
            }
            PlaylistsAddUiState.PLAYLIST_ALREADY_ADDED -> {
                getString(R.string.playlist_already_added)
            }
            else -> {
                ""
            }
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        viewModel.playlistsAddState.postValue(PlaylistsAddUiState.READY)
    }

}