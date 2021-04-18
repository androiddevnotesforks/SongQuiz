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
import com.arpadfodor.android.songquiz.view.utils.AppFragment
import com.arpadfodor.android.songquiz.view.utils.AppInputDialog
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsViewModel

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

        val playlistKey = "37i9dQZF1DX4UtSsGT1Sbe"

        binding.btnStartQuiz.setOnClickListener {
            val intent = Intent(this.requireContext(), QuizActivity::class.java)
            intent.putExtra(QuizActivity.PLAYLIST_KEY, playlistKey)
            startActivity(intent)
        }

        binding.fabAddPlaylist.setOnClickListener {
            getInput()
        }

        viewModel.text.observe(viewLifecycleOwner, {
            binding.textPlaylist.text = it
        })

    }

    override fun subscribeViewModel() {

        val loadingObserver = Observer<Boolean> { loadingState ->
            val text = when(loadingState){
                true -> {
                    getString(R.string.new_playlist_loading)
                }
                false -> {
                    ""
                }
                else -> {
                    ""
                }
            }
            binding.textPlaylist.text = text
        }
        viewModel.loadingState.observe(this, loadingObserver)

    }

    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    private fun getInput() {
        val inputDialog = AppInputDialog(this.requireContext(), getString(R.string.add_new_playlist),
            getString(R.string.add_new_playlist_description))
        inputDialog.setPositiveButton {
            viewModel.addPlaylistById(it)
        }
        inputDialog.show()
    }

}