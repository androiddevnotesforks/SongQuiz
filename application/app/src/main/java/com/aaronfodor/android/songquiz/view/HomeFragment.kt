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
import com.aaronfodor.android.songquiz.databinding.FragmentHomeBinding
import com.aaronfodor.android.songquiz.view.utils.*
import com.aaronfodor.android.songquiz.viewmodel.*
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelPlaylist
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toListable
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class HomeFragment : AppFragment(R.layout.fragment_home), View.OnCreateContextMenuListener, ListableListener {

    private val binding: FragmentHomeBinding by viewBinding()

    override lateinit var viewModel: HomeViewModel

    // listable listener
    override lateinit var primaryListableAction: ListableAction
    override lateinit var secondaryListableAction: ListableAction
    override lateinit var swipeListableAction: ListableAction
    override fun lastListableReached() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupRecyclerView()
    }

    override fun subscribeViewModel() {
        // needed to trigger refresh when UI is shown again
        viewModel.loadData()

        val playlistsObserver = Observer<List<ViewModelPlaylist>> { playlists ->
            val newList = playlists.map { it.toListable() }
            (binding.list.RecyclerView.adapter as ListableAdapter).submitList(newList)

            if(playlists.isEmpty() && (viewModel.uiState.value == HomeUiState.READY)){
                binding.list.tvEmpty.appear(R.anim.slide_in_bottom)
            }
            else{
                binding.list.tvEmpty.disappear(R.anim.slide_out_bottom)
            }
        }
        viewModel.playlists.observe(this, playlistsObserver)

        val uiStateObserver = Observer<HomeUiState> { state ->
            if(state != HomeUiState.LOADING){
                binding.list.swipeRefreshLayout.isRefreshing = false
            }

            when(state){
                HomeUiState.LOADING -> {
                    binding.list.swipeRefreshLayout.isRefreshing = true
                }
                HomeUiState.READY -> {}
                else -> {}
            }
        }
        viewModel.uiState.observe(this, uiStateObserver)

        val notificationObserver = Observer<HomeNotification> { notification ->
            when(notification){
                HomeNotification.SUCCESS_DELETE_PLAYLIST -> {
                    Snackbar.make(binding.root, getString(R.string.success_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(HomeNotification.NONE)
                }
                HomeNotification.ERROR_DELETE_PLAYLIST -> {
                    Snackbar.make(binding.root, getString(R.string.error_listable_delete), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(HomeNotification.NONE)
                }
                HomeNotification.NO_PLAYLISTS -> {
                    Snackbar.make(binding.root, getString(R.string.empty_list_playlists_random), Snackbar.LENGTH_LONG).show()
                    viewModel.notification.postValue(HomeNotification.NONE)
                }
                HomeNotification.START_QUIZ -> {
                    viewModel.notification.postValue(HomeNotification.NONE)
                    showQuizScreen()
                }
                HomeNotification.SHOW_ADD_SCREEN -> {
                    viewModel.notification.postValue(HomeNotification.NONE)
                    showAddPlaylistsScreen()
                }
                HomeNotification.NONE -> {}
                else -> {}
            }
        }
        viewModel.notification.observe(this, notificationObserver)

        binding.tvGreet.isAllCaps = false
        when(viewModel.getPartOfTheDay()){
            PartOfTheDay.MORNING -> {
                binding.tvGreet.text = getString(R.string.greeting_morning)
                val drawable = getDrawable(requireContext(), R.drawable.icon_sun)
                binding.tvGreet.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
            PartOfTheDay.AFTERNOON -> {
                binding.tvGreet.text = getString(R.string.greeting_afternoon)
                val drawable = getDrawable(requireContext(), R.drawable.icon_sun)
                binding.tvGreet.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
            PartOfTheDay.EVENING -> {
                binding.tvGreet.text = getString(R.string.greeting_evening)
                val drawable = getDrawable(requireContext(), R.drawable.icon_sunset)
                binding.tvGreet.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
            PartOfTheDay.NIGHT -> {
                binding.tvGreet.text = getString(R.string.greeting_night)
                val drawable = getDrawable(requireContext(), R.drawable.icon_moon)
                binding.tvGreet.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
            PartOfTheDay.UNKNOWN -> {
                binding.tvGreet.text = getString(R.string.greeting_fallback)
                val drawable = getDrawable(requireContext(), R.drawable.icon_hello)
                binding.tvGreet.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }
        }

        val randomPlay = {
            viewModel.startRandomQuiz()
        }
        val randomPlayText = getString(R.string.random_play)
        val drawable = getDrawable(requireContext(), R.drawable.icon_random)
        binding.actionRandom.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable)
        binding.actionRandom.contentDescription = randomPlayText
        binding.actionRandom.text = randomPlayText
        binding.actionRandom.setOnClickListener { randomPlay() }

    }

    override fun appearingAnimations() {
        binding.tvGreet.appear(R.anim.slide_in_bottom, true)
        binding.actionRandom.appear(R.anim.slide_in_bottom, true)

        if(binding.list.tvEmpty.visibility == View.VISIBLE){
            binding.list.tvEmpty.appear(R.anim.slide_in_bottom)
        }
    }

    override fun unsubscribeViewModel() {

    }

    private fun showInfoScreen(id: String){
        val navController = NavHostFragment.findNavController(this)
        // make sure that the current destination is the current fragment (filter duplicated calls)
        if(navController.currentDestination == navController.findDestination(R.id.nav_home)){
            val action = HomeFragmentDirections.actionNavHomeToNavInfoPlaylist(viewModel.callerType, id)
            navController.navigate(action)
        }
    }

    private fun playByIdSelected(id: String){
        viewModel.play(id)
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
        if(navController.currentDestination == navController.findDestination(R.id.nav_home)){
            val action = HomeFragmentDirections.actionNavHomeToNavAdd()
            navController.navigate(action)
        }
    }

    private fun setupRecyclerView() {
        val startLambda: (Listable) -> Unit = { playlist -> playByIdSelected(playlist.id) }
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
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
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

        // because it is already in a scrollview, make it non-scrollable
        binding.list.RecyclerView.isNestedScrollingEnabled = false

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