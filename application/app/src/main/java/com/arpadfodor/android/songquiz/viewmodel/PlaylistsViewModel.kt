package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    var repository: PlaylistsRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is playlist Fragment"
    }
    val text: LiveData<String> = _text

}