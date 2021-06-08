package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.FragmentPlaylistsBinding
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.view.utils.AppFragment
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsUiState
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class PlaylistsFragment : AppFragment(R.layout.fragment_playlists) {

    private val binding: FragmentPlaylistsBinding by viewBinding()

    private lateinit var viewModel: PlaylistsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaylistsViewModel::class.java)

        val startLambda: (Playlist) -> Unit = { playlist -> startPlaylistById(playlist.id) }
        val deleteLambda: (Playlist) -> Unit = { playlist -> deletePlaylistById(playlist.id, playlist.name) }

        val playlistsAdapter = PlaylistsAdapter(this.requireContext(), startLambda, deleteLambda)
        binding.RecyclerViewPlaylists.adapter = playlistsAdapter

        binding.fabAddPlaylist.setOnClickListener {
            viewModel.showAddPlaylistScreen()
        }
        binding.tvEmpty.setOnClickListener {
            viewModel.showAddPlaylistScreen()
        }
    }

    override fun subscribeViewModel() {
        val playlistsObserver = Observer<List<Playlist>> { playlists ->
            (binding.RecyclerViewPlaylists.adapter as PlaylistsAdapter).submitList(playlists)

            if(playlists.isEmpty() && (viewModel.playlistsState.value == PlaylistsUiState.READY)){
                binding.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.tvEmpty.visibility = View.GONE
            }
        }
        viewModel.playlists.observe(this, playlistsObserver)

        val playlistsStateObserver = Observer<PlaylistsUiState> { state ->

            if(state != PlaylistsUiState.READY){
                binding.tvEmpty.visibility = View.GONE
            }
            if(state != PlaylistsUiState.LOADING){
                binding.loadIndicatorProgressBar.visibility = View.GONE
            }

            when(state){
                PlaylistsUiState.LOADING -> {
                    binding.loadIndicatorProgressBar.visibility = View.VISIBLE
                }
                PlaylistsUiState.READY -> {

                }
                PlaylistsUiState.SHOW_ADD_SCREEN -> {
                    showAddPlaylistsScreen()
                }
                else -> {}
            }

            if(viewModel.playlists.value.isNullOrEmpty() && (state == PlaylistsUiState.READY)){
                binding.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.tvEmpty.visibility = View.GONE
            }
        }
        viewModel.playlistsState.observe(this, playlistsStateObserver)
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun deletePlaylistById(id: String, name: String) {
        val inputDialog = AppDialog(this.requireContext(), getString(R.string.delete_playlist),
                getString(R.string.delete_playlist_description, name), R.drawable.icon_warning)
        inputDialog.setPositiveButton {
            viewModel.deletePlaylistById(id)
        }
        inputDialog.show()
    }

    private fun startPlaylistById(id: String){
        // Log start game event
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.LEVEL_START){
            param(FirebaseAnalytics.Param.ITEM_ID, id)
        }

        val intent = Intent(this.requireContext(), QuizActivity::class.java)
        intent.putExtra(QuizActivity.PLAYLIST_KEY, id)
        startActivity(intent)
    }

    private fun showAddPlaylistsScreen(){
        val navHostFragment = NavHostFragment.findNavController(this)
        navHostFragment.navigate(R.id.to_nav_playlist_add, null)
        viewModel.playlistsState.postValue(PlaylistsUiState.READY)
    }

}