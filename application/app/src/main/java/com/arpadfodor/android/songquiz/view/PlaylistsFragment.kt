package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.FragmentPlaylistsBinding
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.view.utils.AppDialogInput
import com.arpadfodor.android.songquiz.view.utils.AppFragment
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsUiState
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class PlaylistsFragment : AppFragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private lateinit var viewModel: PlaylistsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(PlaylistsViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startLambda: (Playlist) -> Unit = { playlist -> startPlaylistById(playlist.id) }
        val deleteLambda: (Playlist) -> Unit = { playlist -> deletePlaylistById(playlist.id, playlist.name) }

        val playlistsAdapter = PlaylistsAdapter(this.requireContext(), startLambda, deleteLambda)
        binding.RecyclerViewPlaylists.adapter = playlistsAdapter

        binding.fabAddPlaylist.setOnClickListener {
            getSearchExpression()
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
                PlaylistsUiState.CANNOT_FIND_PLAYLIST -> {
                    showInfo(PlaylistsUiState.CANNOT_FIND_PLAYLIST)
                }
                else -> {}
            }
        }
        viewModel.playlistsState.observe(this, playlistsStateObserver)
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun getSearchExpression() {
        val inputDialog = AppDialogInput(this.requireContext(), getString(R.string.add_playlist),
            getString(R.string.add_playlist_description))
        inputDialog.setPositiveButton {
            viewModel.searchPlaylistsByIdOrName(it)
        }
        inputDialog.show()
    }

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
        // this check is needed to prevent illegal navigation state exception
        val navHostFragment = NavHostFragment.findNavController(this)
        if (navHostFragment.currentDestination?.id != R.id.nav_playlist_add) {
            navHostFragment.navigate(R.id.action_nav_playlists_to_nav_playlist_add, null)
            viewModel.playlistsState.postValue(PlaylistsUiState.READY)
        }
    }

    private fun showInfo(infoType: PlaylistsUiState){

        when(infoType){
            PlaylistsUiState.CANNOT_FIND_PLAYLIST -> {
                val message = getString(R.string.cannot_find_playlist)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.playlistsState.postValue(PlaylistsUiState.READY)
            }
            else -> {}
        }

    }

}