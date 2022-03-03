package com.aaronfodor.android.songquiz.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.FragmentPlaylistsBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsNotification
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsUiState
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsViewModel
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toListable
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class PlaylistsFragment : AppFragment(R.layout.fragment_playlists), View.OnCreateContextMenuListener, ListableListener {

    private val binding: FragmentPlaylistsBinding by viewBinding()

    override lateinit var viewModel: PlaylistsViewModel

    // listable listener
    override lateinit var primaryListableAction: ListableAction
    override lateinit var secondaryListableAction: ListableAction
    override lateinit var swipeListableAction: ListableAction
    override fun lastListableReached() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PlaylistsViewModel::class.java]
        setupRecyclerView()
    }

    override fun subscribeViewModel() {
        // needed to trigger refresh when UI is shown again
        viewModel.loadData()

        val playlistsObserver = Observer<List<ViewModelPlaylist>> { playlists ->
            val newList = playlists.map { it.toListable() }
            (binding.list.RecyclerView.adapter as ListableAdapter).submitList(newList)

            if(playlists.isEmpty() && (viewModel.uiState.value == PlaylistsUiState.READY)){
                binding.list.tvEmpty.appear(R.anim.slide_in_bottom)
            }
            else{
                binding.list.tvEmpty.disappear(R.anim.slide_out_bottom)
            }
        }
        viewModel.playlists.observe(this, playlistsObserver)

        val uiStateObserver = Observer<PlaylistsUiState> { state ->
            if(state != PlaylistsUiState.READY){
                binding.list.tvEmpty.disappear(R.anim.slide_out_bottom)
            }
            if(state != PlaylistsUiState.LOADING){
                binding.list.swipeRefreshLayout.isRefreshing = false
            }

            when(state){
                PlaylistsUiState.LOADING -> {
                    binding.list.swipeRefreshLayout.isRefreshing = true
                }
                PlaylistsUiState.READY -> {}
                else -> {}
            }

            if(viewModel.playlists.value.isNullOrEmpty() && (state == PlaylistsUiState.READY)){
                binding.list.tvEmpty.appear(R.anim.slide_in_bottom)
            }
            else{
                binding.list.tvEmpty.disappear(R.anim.slide_out_bottom)
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val notificationObserver = Observer<PlaylistsNotification> { notification ->
            when(notification){
                PlaylistsNotification.SUCCESS_DELETE_PLAYLIST -> {
                    Snackbar.make(binding.root, getString(R.string.success_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(PlaylistsNotification.NONE)
                }
                PlaylistsNotification.ERROR_DELETE_PLAYLIST -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(PlaylistsNotification.NONE)
                }
                PlaylistsNotification.START_QUIZ -> {
                    viewModel.notification.postValue(PlaylistsNotification.NONE)
                    showQuizScreen()
                }
                PlaylistsNotification.SHOW_ADD_SCREEN -> {
                    viewModel.notification.postValue(PlaylistsNotification.NONE)
                    showAddPlaylistsScreen()
                }
                PlaylistsNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)
    }

    override fun appearingAnimations() {
        binding.fabAddPlaylist.appear(R.anim.slide_in_right, true)

        if(binding.list.tvEmpty.visibility == View.VISIBLE){
            binding.list.tvEmpty.appear(R.anim.slide_in_bottom)
        }
    }

    override fun unsubscribeViewModel() {}

    private fun showInfoScreen(id: String){
        val navController = NavHostFragment.findNavController(this)
        // make sure that the current destination is the current fragment (filter duplicated calls)
        if(navController.currentDestination == navController.findDestination(R.id.nav_play)){
            val action = PlaylistsFragmentDirections.actionNavPlayToNavInfoPlaylist(viewModel.callerType, id)
            navController.navigate(action)
        }
    }

    private fun playlistByIdSelected(id: String){
        // record the selected playlist
        viewModel.startQuiz(id)
    }

    private fun deletePlaylist(name: String, id: String, dismissAction: () -> Unit) {
        val dialog = AppDialog(this.requireContext(), getString(R.string.delete_playlist),
            getString(R.string.delete_playlist_description, name), R.drawable.icon_delete)
        dialog.setPositiveButton {
            viewModel.deletePlaylist(id)
        }
        dialog.setNegativeButton {
            dismissAction()
        }
        dialog.setOnDismissListener {
            dismissAction()
        }
        dialog.show()
    }

    private fun showQuizScreen(){
        // Log start game event
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.LEVEL_START){
            param(FirebaseAnalytics.Param.ITEM_ID, viewModel.selectedPlaylistId)
        }

        val intent = Intent(this.requireContext(), QuizActivity::class.java)
        intent.putExtra(QuizActivity.PLAYLIST_KEY, viewModel.selectedPlaylistId)
        startActivity(intent)
    }

    private fun showAddPlaylistsScreen(){
        val navController = NavHostFragment.findNavController(this)
        // make sure that the current destination is the current fragment (filter duplicated calls)
        if(navController.currentDestination == navController.findDestination(R.id.nav_play)){
            val action = PlaylistsFragmentDirections.actionNavPlayToNavAdd()
            navController.navigate(action)
        }
    }

    private fun setupRecyclerView() {
        val startLambda: (Listable) -> Unit = { playlist -> playlistByIdSelected(playlist.id) }
        val startDrawable = getDrawable(requireContext(), R.drawable.icon_start_game)
        val startText = getString(R.string.play)

        val infoLambda: (Listable) -> Unit = { playlist -> showInfoScreen(playlist.id) }
        val infoDrawable = getDrawable(requireContext(), R.drawable.icon_info)
        val infoText = getString(R.string.info)

        val deleteLambda: (Listable) -> Unit = { playlist -> deletePlaylist(playlist.title, playlist.id, {}) }
        val deleteDrawable = getDrawable(requireContext(), R.drawable.icon_delete)
        val deleteText = getString(R.string.delete)

        primaryListableAction = ListableAction(startLambda, startDrawable, startText)
        secondaryListableAction = ListableAction(infoLambda, infoDrawable, infoText)
        swipeListableAction = ListableAction(deleteLambda, deleteDrawable, deleteText)

        val listAdapter = ListableAdapter()
        listAdapter.listableListener = this
        binding.list.RecyclerView.adapter = listAdapter
        //swipe
        binding.list.RecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        var itemTouchHelper: ItemTouchHelper? = null
        itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                viewModel.playlists.value?.let {
                    val playlist = it[position].toListable()
                    deletePlaylist(playlist.title, playlist.id, dismissAction = {
                        // bring back the swiped item
                        itemTouchHelper?.attachToRecyclerView(null)
                        itemTouchHelper?.attachToRecyclerView(binding.list.RecyclerView)
                    })
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.list.RecyclerView)
        registerForContextMenu(binding.list.RecyclerView)

        binding.fabAddPlaylist.setOnClickListener {
            viewModel.showAddPlaylistScreen()
        }
        binding.list.tvEmpty.setOnClickListener {
            viewModel.showAddPlaylistScreen()
        }
        binding.list.tvEmpty.text = getText(R.string.empty_list_playlists)
        val drawable = getDrawable(requireContext(), R.drawable.icon_add_playlist)
        binding.list.tvEmpty.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)

        binding.list.swipeRefreshLayout.setColorSchemeColors(getColor(requireContext(), R.color.colorAccent))
        binding.list.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadData()
        }
    }

}