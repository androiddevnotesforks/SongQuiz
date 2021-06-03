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
import com.arpadfodor.android.songquiz.model.repository.dataclasses.SearchResult
import com.arpadfodor.android.songquiz.view.utils.AppDialogInput
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
        val lastItemLambda: () -> Unit = { searchGetNextBatch() }

        val playlistsAdapter = PlaylistAddAdapter(this.requireContext(), addLambda, lastItemLambda)
        binding.RecyclerViewPlaylists.adapter = playlistsAdapter

        binding.fabSearch.setOnClickListener{
            showSearchExpressionDialog()
        }
    }

    override fun subscribeViewModel() {
        viewModel.setPlaylistIdsAlreadyAdded()

        val playlistsFoundObserver = Observer<SearchResult> { result ->
            (binding.RecyclerViewPlaylists.adapter as PlaylistAddAdapter).submitList(result.items)

            if(result.items.isEmpty() && (viewModel.playlistsAddState.value == PlaylistsAddUiState.READY)){
                binding.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.tvEmpty.visibility = View.GONE
            }
        }
        viewModel.searchResult.observe(this, playlistsFoundObserver)

        val playlistsAddStateObserver = Observer<PlaylistsAddUiState> { state ->

            if(state != PlaylistsAddUiState.LOADING){
                binding.loadIndicatorProgressBar.visibility = View.GONE
            }

            when(state){
                PlaylistsAddUiState.LOADING -> {
                    binding.loadIndicatorProgressBar.visibility = View.VISIBLE
                }
                PlaylistsAddUiState.READY -> {}
                PlaylistsAddUiState.NOT_FOUND -> {
                    showInfo(PlaylistsAddUiState.NOT_FOUND)
                }
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

            if(viewModel.searchResult.value?.items.isNullOrEmpty() && (state == PlaylistsAddUiState.READY)){
                binding.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.tvEmpty.visibility = View.GONE
            }
        }
        viewModel.playlistsAddState.observe(this, playlistsAddStateObserver)
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun showSearchExpressionDialog() {
        val inputDialog = AppDialogInput(this.requireContext(), getString(R.string.search_playlist),
            getString(R.string.search_playlist_description))
        inputDialog.setPositiveButton {
            viewModel.searchPlaylistByIdOrName(it)
        }
        inputDialog.show()
    }

    private fun searchGetNextBatch(){
        viewModel.searchGetNextBatch()
    }

    private fun addPlaylist(id: String){
        viewModel.addPlaylistById(id)
    }

    private fun showInfo(infoType: PlaylistsAddUiState){
        val message = when(infoType){
            PlaylistsAddUiState.NOT_FOUND -> {
                getString(R.string.cannot_find_playlist)
            }
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