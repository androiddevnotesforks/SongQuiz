package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel  @Inject constructor(
    var repository: PlaylistsRepository
) : ViewModel() {

    fun deletePlaylists(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlaylists()
        }
    }

}