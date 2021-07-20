package com.aaronfodor.android.songquiz.view

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentPlaylistAddBinding
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.model.repository.dataclasses.PlaylistSearchResult
import com.aaronfodor.android.songquiz.view.utils.AppDialogInput
import com.aaronfodor.android.songquiz.view.utils.AppFragment
import com.aaronfodor.android.songquiz.view.utils.AuthRequestContract
import com.aaronfodor.android.songquiz.view.utils.AuthRequestModule
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsAddUiState
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsAddViewModel
import com.google.android.material.snackbar.Snackbar

class PlaylistAddFragment : AppFragment(R.layout.fragment_playlist_add), AuthRequestModule {

    private val binding: FragmentPlaylistAddBinding by viewBinding()

    private lateinit var viewModel: PlaylistsAddViewModel

    private var lastSearchExpression = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaylistsAddViewModel::class.java)

        val addLambda: (Playlist) -> Unit = { playlist -> addPlaylist(playlist.id) }
        val infoLambda: (Playlist) -> Unit = { playlist -> showInfoScreen(playlist.id) }

        val addText = getString(R.string.unicode_add)
        val infoDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.icon_info)

        val lastItemLambda: () -> Unit = { searchGetNextBatch() }

        val playlistsAdapter = PlaylistsAdapter(this.requireContext(), addLambda, addText, infoLambda, infoDrawable, lastItemLambda)
        binding.RecyclerViewPlaylists.adapter = playlistsAdapter

        binding.fabSearch.setOnClickListener{ showSearchExpressionDialog() }
        binding.tvEmpty.setOnClickListener { showSearchExpressionDialog() }
    }

    override fun subscribeViewModel() {
        viewModel.setPlaylistIdsAlreadyAdded()

        val playlistsFoundObserver = Observer<PlaylistSearchResult> { result ->
            (binding.RecyclerViewPlaylists.adapter as PlaylistsAdapter).submitList(result.items)

            if(result.items.isEmpty() && (viewModel.uiState.value == PlaylistsAddUiState.READY)){
                binding.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.tvEmpty.visibility = View.GONE
            }
        }
        viewModel.searchResult.observe(this, playlistsFoundObserver)

        val uiStateObserver = Observer<PlaylistsAddUiState> { state ->

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
                PlaylistsAddUiState.AUTH_NEEDED -> {
                    startAuthentication()
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
        viewModel.uiState.observe(this, uiStateObserver)
    }

    override fun appearingAnimations() {
        val rightAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
        binding.fabSearch.startAnimation(rightAnimation)
        binding.fabSearch.visibility = View.VISIBLE
    }

    override fun unsubscribeViewModel() {}

    private fun showSearchExpressionDialog() {
        val inputDialog = AppDialogInput(this.requireContext(), getString(R.string.search_playlist),
            getString(R.string.search_playlist_description))
        inputDialog.setPositiveButton {
            lastSearchExpression = it
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

    private fun showInfoScreen(id: String){
        val navHostFragment = NavHostFragment.findNavController(this)
        val action = PlaylistsFragmentDirections.toNavInfoFromAddPlaylists(id)
        navHostFragment.navigate(action)
        viewModel.ready()
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
        viewModel.ready()
    }

    override var authLauncherStarted = false
    override val authLauncher = registerForActivityResult(AuthRequestContract()){ isAuthSuccess ->
        if(isAuthSuccess){
            viewModel.searchPlaylistByIdOrName(lastSearchExpression)
        }
        else{
            viewModel.ready()
        }
        authLauncherStarted = false
    }

}