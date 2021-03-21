package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.android.songquiz.databinding.FragmentPlaylistsBinding
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsViewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private lateinit var viewModel: PlaylistsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(PlaylistsViewModel::class.java)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartQuiz.setOnClickListener {
            startActivity(Intent(this.requireContext(), QuizActivity::class.java))
        }

        viewModel.text.observe(viewLifecycleOwner, Observer {
            binding.textPlaylist.text = it
        })

    }

}