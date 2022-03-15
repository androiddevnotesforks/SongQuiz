package com.aaronfodor.android.songquiz.view

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
import com.aaronfodor.android.songquiz.databinding.FragmentPlaylistAddBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsAddNotification
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsAddUiState
import com.aaronfodor.android.songquiz.viewmodel.PlaylistsAddViewModel
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylistSearchResult
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.removeIds
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toListable
import com.google.android.material.snackbar.Snackbar

class PlaylistAddFragment : AppFragment(R.layout.fragment_playlist_add), ListableListener {

    private val binding: FragmentPlaylistAddBinding by viewBinding()

    override lateinit var viewModel: PlaylistsAddViewModel

    // listable listener
    override lateinit var primaryListableAction: ListableAction
    override lateinit var secondaryListableAction: ListableAction
    override lateinit var swipeListableAction: ListableAction
    override fun lastListableReached() {
        searchGetNextBatch()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PlaylistsAddViewModel::class.java]
        setupRecyclerView()
    }

    override fun subscribeViewModel() {
        viewModel.setPlaylistIdsAlreadyAdded()

        val playlistsFoundObserver = Observer<ViewModelPlaylistSearchResult> { searchResult ->
            searchResult?.let { result ->
                (binding.list.RecyclerView.adapter as ListableAdapter).submitList(result.items.map { it.toListable() })

                if(result.items.isEmpty() && (viewModel.uiState.value == PlaylistsAddUiState.READY)){
                    binding.list.tvEmpty.appear(R.anim.slide_in_bottom)
                }
                else{
                    binding.list.tvEmpty.disappear(R.anim.slide_out_bottom)
                }
            }
        }
        viewModel.searchResult.observe(this, playlistsFoundObserver)

        val uiStateObserver = Observer<PlaylistsAddUiState> { state ->
            if(state != PlaylistsAddUiState.LOADING){
                binding.list.swipeRefreshLayout.isRefreshing = false
            }

            when(state){
                PlaylistsAddUiState.LOADING -> {
                    binding.list.swipeRefreshLayout.isRefreshing = true
                }
                PlaylistsAddUiState.READY -> {}
                else -> {}
            }

            if(viewModel.searchResult.value?.items.isNullOrEmpty() && (state == PlaylistsAddUiState.READY)){
                binding.list.tvEmpty.appear(R.anim.slide_in_bottom)
            }
            else{
                binding.list.tvEmpty.disappear(R.anim.slide_out_bottom)
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val notificationObserver = Observer<PlaylistsAddNotification> { notification ->
            when(notification){
                PlaylistsAddNotification.NOT_FOUND -> {
                    Snackbar.make(binding.root, getString(R.string.cannot_find_listable), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(PlaylistsAddNotification.NONE)
                }
                PlaylistsAddNotification.SUCCESS_ADD_PLAYLIST -> {
                    Snackbar.make(binding.root, getString(R.string.success_listable_add), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(PlaylistsAddNotification.NONE)
                }
                PlaylistsAddNotification.ERROR_ADD_PLAYLIST -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_add), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(PlaylistsAddNotification.NONE)
                }
                PlaylistsAddNotification.PLAYLIST_ALREADY_ADDED -> {
                    Snackbar.make(binding.root, getString(R.string.listable_already_added), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(PlaylistsAddNotification.NONE)
                }
                PlaylistsAddNotification.DISCARDED -> {
                    Snackbar.make(binding.root, getString(R.string.listable_discarded), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(PlaylistsAddNotification.NONE)
                }
                PlaylistsAddNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)
    }

    override fun appearingAnimations() {
        binding.fabSearch.appear(R.anim.slide_in_right, true)

        if(binding.list.tvEmpty.visibility == View.VISIBLE){
            binding.list.tvEmpty.appear(R.anim.slide_in_bottom)
        }
    }

    override fun unsubscribeViewModel() {}

    private fun showSearchExpressionDialog() {
        val inputDialog = AppDialogInput(this.requireContext(), getString(R.string.search_playlist),
            getString(R.string.search_playlist_description))
        inputDialog.setPositiveButton {
            viewModel.searchPlaylist(it)
        }
        inputDialog.show()
    }

    private fun searchGetNextBatch(){
        viewModel.getNextBatch()
    }

    private fun addPlaylist(id: String){
        viewModel.addPlaylistById(id)
    }

    private fun showInfoScreen(id: String){
        val navController = NavHostFragment.findNavController(this)
        if(navController.currentDestination == navController.findDestination(R.id.nav_add)){
            val action = PlaylistAddFragmentDirections.actionNavAddToNavInfoPlaylist(viewModel.callerType, id)
            navController.navigate(action)
        }
    }

    private fun setupRecyclerView(){
        val addLambda: (Listable) -> Unit = { playlist -> addPlaylist(playlist.id) }
        val addDrawable = getDrawable(requireContext(), R.drawable.icon_add_circular)
        val addText = getString(R.string.add)

        val infoLambda: (Listable) -> Unit = { playlist -> showInfoScreen(playlist.id) }
        val infoDrawable = getDrawable(requireContext(), R.drawable.icon_info)
        val infoText = getString(R.string.info)

        primaryListableAction = ListableAction(addLambda, addDrawable, addText)
        secondaryListableAction = ListableAction(infoLambda, infoDrawable, infoText)
        swipeListableAction = ListableAction({}, null, "")

        val listAdapter = ListableAdapter()
        listAdapter.listableListener = this
        binding.list.RecyclerView.adapter = listAdapter
        //swipe
        binding.list.RecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                viewModel.searchResult.value?.let {
                    val idToRemove = it.items[position].id
                    viewModel.searchResult.postValue(it.removeIds(listOf(idToRemove)))
                    viewModel.notification.postValue(PlaylistsAddNotification.DISCARDED)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.list.RecyclerView)
        registerForContextMenu(binding.list.RecyclerView)

        binding.fabSearch.setOnClickListener{ showSearchExpressionDialog() }
        binding.list.tvEmpty.setOnClickListener { showSearchExpressionDialog() }
        binding.list.tvEmpty.text = getText(R.string.empty_search_list)
        val drawable = getDrawable(requireContext(), R.drawable.icon_search)
        binding.list.tvEmpty.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)

        binding.list.swipeRefreshLayout.setColorSchemeColors(getColor(requireContext(), R.color.colorPrimary))
        binding.list.swipeRefreshLayout.setOnRefreshListener {
            viewModel.searchPlaylist(viewModel.lastSearchExpression)
        }
    }

}