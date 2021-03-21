package com.arpadfodor.android.songquiz.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.viewmodel.PlaylistsViewModel

class PlaylistsFragment : Fragment() {

    private lateinit var playlistsViewModel: PlaylistsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        playlistsViewModel = ViewModelProvider(this).get(PlaylistsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_playlists, container, false)
        val textView: TextView = root.findViewById(R.id.text_playlist)
        val startQuizButton: Button = root.findViewById(R.id.btnStartQuiz)

        startQuizButton.setOnClickListener {
            startActivity(Intent(this.requireContext(), QuizActivity::class.java))
        }

        playlistsViewModel.text.observe(viewLifecycleOwner, Observer { textView.text = it })
        return root
    }

}