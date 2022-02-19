package com.aaronfodor.android.songquiz.view

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
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
import com.aaronfodor.android.songquiz.databinding.FragmentFavouritesBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.FavouritesNotification
import com.aaronfodor.android.songquiz.viewmodel.FavouritesUiState
import com.aaronfodor.android.songquiz.viewmodel.FavouritesViewModel
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toListable
import com.google.android.material.snackbar.Snackbar

class FavouritesFragment : AppFragment(R.layout.fragment_favourites), View.OnCreateContextMenuListener, ListableListener {

    private val binding: FragmentFavouritesBinding by viewBinding()

    override lateinit var viewModel: FavouritesViewModel

    // listable listener
    override lateinit var primaryListableAction: ListableAction
    override lateinit var secondaryListableAction: ListableAction
    override lateinit var swipeListableAction: ListableAction
    override fun lastListableReached() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FavouritesViewModel::class.java]
        setupRecyclerView()
    }

    override fun subscribeViewModel() {
        // needed to trigger refresh when UI is shown again
        viewModel.loadData()

        val tracksObserver = Observer<List<ViewModelPlaylist>> { tracks ->
            val newList = tracks.map { it.toListable() }
            (binding.list.RecyclerView.adapter as ListableAdapter).submitList(newList)

            if(tracks.isEmpty() && (viewModel.uiState.value == FavouritesUiState.READY)){
                binding.list.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.list.tvEmpty.visibility = View.INVISIBLE
            }
        }
        viewModel.tracks.observe(this, tracksObserver)

        val uiStateObserver = Observer<FavouritesUiState> { state ->
            if(state != FavouritesUiState.READY){
                binding.list.tvEmpty.visibility = View.INVISIBLE
            }
            if(state != FavouritesUiState.LOADING){
                binding.list.swipeRefreshLayout.isRefreshing = false
            }

            when(state){
                FavouritesUiState.LOADING -> {
                    binding.list.swipeRefreshLayout.isRefreshing = true
                }
                FavouritesUiState.READY -> {
                    binding.list.tvEmpty.visibility = View.VISIBLE
                }
                else -> {}
            }

            if(viewModel.tracks.value.isNullOrEmpty() && (state == FavouritesUiState.READY)){
                binding.list.tvEmpty.visibility = View.VISIBLE
            }
            else{
                binding.list.tvEmpty.visibility = View.INVISIBLE
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val notificationObserver = Observer<FavouritesNotification> { notification ->
            when(notification){
                FavouritesNotification.SUCCESS_DELETE_TRACK -> {
                    Snackbar.make(binding.root, getString(R.string.success_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(FavouritesNotification.NONE)
                }
                FavouritesNotification.ERROR_DELETE_TRACK -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(FavouritesNotification.NONE)
                }
                FavouritesNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)
    }

    override fun appearingAnimations() {
        val bottomAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom)
        binding.list.tvEmpty.startAnimation(bottomAnimation)
    }

    override fun unsubscribeViewModel() {}

    private fun showInfoScreen(id: String){
        val navController = NavHostFragment.findNavController(this)
        val action = FavouritesFragmentDirections.actionNavFavouritesToNavInfoTrack(viewModel.callerType, id)
        navController.navigate(action)
        viewModel.ready()
    }

    private fun deleteTrack(name: String, id: String, dismissAction: () -> Unit) {
        val dialog = AppDialog(this.requireContext(), getString(R.string.delete_track),
            getString(R.string.delete_track_description, name), R.drawable.icon_delete)
        dialog.setPositiveButton {
            viewModel.deleteTrack(id)
        }
        dialog.setNegativeButton {
            dismissAction()
        }
        dialog.setOnDismissListener {
            dismissAction()
        }
        dialog.show()
    }

    private fun setupRecyclerView() {
        val listenLambda: (Listable) -> Unit = { track -> }
        val listenDrawable = getDrawable(requireContext(), R.drawable.icon_play)
        val listenText = getString(R.string.listen)

        val infoLambda: (Listable) -> Unit = { track -> showInfoScreen(track.id) }
        val infoDrawable = getDrawable(requireContext(), R.drawable.icon_info)
        val infoText = getString(R.string.info)

        val deleteLambda: (Listable) -> Unit = { track -> deleteTrack(track.title, track.id, {}) }
        val deleteDrawable = getDrawable(requireContext(), R.drawable.icon_delete)
        val deleteText = getString(R.string.delete)

        primaryListableAction = ListableAction(listenLambda, listenDrawable, listenText)
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
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                viewModel.tracks.value?.let {
                    val track = it[position].toListable()
                    deleteTrack(track.title, track.id, dismissAction = {
                        // bring back the swiped item
                        itemTouchHelper?.attachToRecyclerView(null)
                        itemTouchHelper?.attachToRecyclerView(binding.list.RecyclerView)
                    })
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.list.RecyclerView)
        registerForContextMenu(binding.list.RecyclerView)

        binding.list.tvEmpty.text = getText(R.string.empty_list_tracks)
        val drawable = getDrawable(requireContext(), R.drawable.icon_favourite)
        binding.list.tvEmpty.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)

        binding.list.swipeRefreshLayout.setColorSchemeColors(getColor(requireContext(), R.color.colorAccent))
        binding.list.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadData()
        }
    }

}