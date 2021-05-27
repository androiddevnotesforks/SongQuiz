package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.FragmentPlaylistsBinding
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.arpadfodor.android.songquiz.view.utils.AppDialog
import com.arpadfodor.android.songquiz.view.utils.AppFragment
import com.arpadfodor.android.songquiz.view.utils.AppInputDialog
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsAdapter
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsState
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

        val playlistsAdapter = PlaylistsAdapter(startLambda, deleteLambda)
        binding.RecyclerViewPlaylists.adapter = playlistsAdapter

        binding.fabAddPlaylist.setOnClickListener {
            getNewPlaylistId()
        }
    }

    override fun subscribeViewModel() {
        val playlistsObserver = Observer<List<Playlist>> { playlists ->
            (binding.RecyclerViewPlaylists.adapter as PlaylistsAdapter).submitList(playlists)
        }
        viewModel.playlists.observe(this, playlistsObserver)

        val playlistsStateObserver = Observer<PlaylistsState> { playlistsState ->
            when(playlistsState){
                PlaylistsState.LOADING -> {
                    binding.loadIndicatorProgressBar.visibility = View.VISIBLE
                }
                PlaylistsState.READY -> {
                    binding.loadIndicatorProgressBar.visibility = View.GONE
                }
                PlaylistsState.ERROR_PLAYLIST_ADD -> {
                    binding.loadIndicatorProgressBar.visibility = View.GONE
                    showError(PlaylistsState.ERROR_PLAYLIST_ADD)
                }
                else -> {
                    binding.loadIndicatorProgressBar.visibility = View.GONE
                }
            }
        }
        viewModel.playlistsState.observe(this, playlistsStateObserver)
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun getNewPlaylistId() {
        val inputDialog = AppInputDialog(this.requireContext(), getString(R.string.add_playlist),
            getString(R.string.add_playlist_description))
        inputDialog.setPositiveButton {
            viewModel.addPlaylistById(it)
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

    private fun showError(errorType: PlaylistsState){

        val errorMessage = when(errorType){
            PlaylistsState.ERROR_PLAYLIST_ADD -> {
                getString(R.string.error_playlist_add)
            }
            else -> {
                ""
            }
        }
        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
        viewModel.playlistsState.postValue(PlaylistsState.READY)

    }

}