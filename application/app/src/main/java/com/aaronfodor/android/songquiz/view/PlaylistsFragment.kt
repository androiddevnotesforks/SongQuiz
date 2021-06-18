package com.aaronfodor.android.songquiz.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentPlaylistsBinding
import com.aaronfodor.android.songquiz.model.repository.dataclasses.Playlist
import com.aaronfodor.android.songquiz.view.utils.AppDialog
import com.aaronfodor.android.songquiz.view.utils.AppFragment
import com.aaronfodor.android.songquiz.view.utils.AuthRequestContract
import com.aaronfodor.android.songquiz.view.utils.AuthRequestModule
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsUiState
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class PlaylistsFragment : AppFragment(R.layout.fragment_playlists), AuthRequestModule {

    private val binding: FragmentPlaylistsBinding by viewBinding()

    private lateinit var viewModel: PlaylistsViewModel

    private var selectedPlaylistId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaylistsViewModel::class.java)

        val startLambda: (Playlist) -> Unit = { playlist -> playlistByIdSelected(playlist.id) }
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

            if(playlists.isEmpty() && (viewModel.uiState.value == PlaylistsUiState.READY)){
                binding.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.tvEmpty.visibility = View.GONE
            }
        }
        viewModel.playlists.observe(this, playlistsObserver)

        val uiStateObserver = Observer<PlaylistsUiState> { state ->

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
                PlaylistsUiState.READY -> {}
                PlaylistsUiState.AUTH_NEEDED -> {
                    startAuthentication()
                }
                PlaylistsUiState.START_QUIZ -> {
                    showQuizScreen()
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
        viewModel.uiState.observe(this, uiStateObserver)
    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun deletePlaylistById(id: String, name: String) {
        val dialog = AppDialog(this.requireContext(), getString(R.string.delete_playlist),
                getString(R.string.delete_playlist_description, name), R.drawable.icon_delete)
        dialog.setPositiveButton {
            viewModel.deletePlaylistById(id)
        }
        dialog.show()
    }

    private fun playlistByIdSelected(id: String){
        // record the selected playlist
        selectedPlaylistId = id
        viewModel.startQuiz()
    }

    private fun showQuizScreen(){
        viewModel.ready()

        // Log start game event
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.LEVEL_START){
            param(FirebaseAnalytics.Param.ITEM_ID, selectedPlaylistId)
        }

        val intent = Intent(this.requireContext(), QuizActivity::class.java)
        intent.putExtra(QuizActivity.PLAYLIST_KEY, selectedPlaylistId)
        startActivity(intent)
    }

    private fun showAddPlaylistsScreen(){
        val navHostFragment = NavHostFragment.findNavController(this)
        navHostFragment.navigate(R.id.to_nav_playlist_add, null)
        viewModel.ready()
    }

    override var authLauncherStarted = false
    override val authLauncher = registerForActivityResult(AuthRequestContract()){ isAuthSuccess ->
        if(isAuthSuccess){
            viewModel.startQuiz()
        }
        else{
            viewModel.ready()
        }
        authLauncherStarted = false
    }

}