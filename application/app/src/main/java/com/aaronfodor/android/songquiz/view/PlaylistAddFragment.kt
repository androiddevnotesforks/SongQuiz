package com.aaronfodor.android.songquiz.view

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
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

class PlaylistAddFragment : AppFragment(R.layout.fragment_playlist_add), AuthRequestModule {

    private val binding: FragmentPlaylistAddBinding by viewBinding()

    private lateinit var viewModel: PlaylistsAddViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PlaylistsAddViewModel::class.java]

        val addLambda: (Listable) -> Unit = { playlist -> addPlaylist(playlist.id) }
        val addDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.icon_add_circular)
        val addText = getString(R.string.add)

        val infoLambda: (Listable) -> Unit = { playlist -> showInfoScreen(playlist.id) }
        val infoDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.icon_info)
        val infoText = getString(R.string.info)

        val primaryAction = ListableAction(addLambda, addDrawable, addText)
        val secondaryAction = ListableAction(infoLambda, infoDrawable, infoText)
        val swipeAction = ListableAction({}, null, "")

        val lastItemLambda: () -> Unit = { searchGetNextBatch() }

        val listAdapter = ListableAdapter(this.requireContext(), primaryAction, secondaryAction, swipeAction, lastItemLambda)
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
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.icon_search)
        binding.list.tvEmpty.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)
    }

    override fun subscribeViewModel() {
        viewModel.setPlaylistIdsAlreadyAdded()

        val playlistsFoundObserver = Observer<ViewModelPlaylistSearchResult> { searchResult ->
            searchResult?.let { result ->
                (binding.list.RecyclerView.adapter as ListableAdapter).submitList(result.items.map { it.toListable() })

                if(result.items.isEmpty() && (viewModel.uiState.value == PlaylistsAddUiState.READY)){
                    binding.list.tvEmpty.visibility = View.VISIBLE
                }
                else{
                    binding.list.tvEmpty.visibility = View.GONE
                }
            }
        }
        viewModel.searchResult.observe(this, playlistsFoundObserver)

        val uiStateObserver = Observer<PlaylistsAddUiState> { state ->
            if(state != PlaylistsAddUiState.LOADING){
                binding.list.loadIndicatorProgressBar.visibility = View.GONE
            }

            when(state){
                PlaylistsAddUiState.LOADING -> {
                    binding.list.loadIndicatorProgressBar.visibility = View.VISIBLE
                }
                PlaylistsAddUiState.AUTH_NEEDED -> {
                    startAuthentication()
                }
                PlaylistsAddUiState.READY -> {}
                else -> {}
            }

            if(viewModel.searchResult.value?.items.isNullOrEmpty() && (state == PlaylistsAddUiState.READY)){
                binding.list.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.list.tvEmpty.visibility = View.GONE
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
        val rightAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right)
        binding.fabSearch.startAnimation(rightAnimation)
        binding.fabSearch.visibility = View.VISIBLE

        val bottomAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom)
        binding.list.tvEmpty.startAnimation(bottomAnimation)
    }

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

    private fun showInfoScreen(id: String){
        val navController = NavHostFragment.findNavController(this)
        val action = PlaylistAddFragmentDirections.actionNavAddToNavInfoPlaylist(viewModel.callerType, id)
        navController.navigate(action)
        viewModel.ready()
    }

    override var authLauncherStarted = false
    override val authLauncher = registerForActivityResult(AuthRequestContract()){ isAuthSuccess ->
        if(isAuthSuccess){
            viewModel.searchPlaylistByIdOrName(viewModel.lastSearchExpression)
        }
        else{
            viewModel.ready()
        }
        authLauncherStarted = false
    }

}