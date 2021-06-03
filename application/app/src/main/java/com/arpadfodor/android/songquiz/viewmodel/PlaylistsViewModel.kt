package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import com.arpadfodor.android.songquiz.model.repository.dataclasses.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlaylistsUiState{
    LOADING, READY, SHOW_ADD_SCREEN
}

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    var repository: PlaylistsRepository
) : ViewModel() {

    val playlists : MutableLiveData<List<Playlist>> by lazy {
        MutableLiveData<List<Playlist>>()
    }

    val playlistsState: MutableLiveData<PlaylistsUiState> by lazy {
        MutableLiveData<PlaylistsUiState>()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            playlistsState.postValue(PlaylistsUiState.LOADING)
            playlists.postValue(repository.getPlaylists())
            playlistsState.postValue(PlaylistsUiState.READY)
        }
    }

    fun showAddPlaylistScreen(){
        val playlistIdsAlreadyAdded : List<String> = playlists.value?.map { it -> it.id } ?: listOf()
        PlaylistsAddViewModel.transferPlaylistIdsAlreadyAdded = playlistIdsAlreadyAdded
        playlistsState.postValue(PlaylistsUiState.SHOW_ADD_SCREEN)
    }

    fun deletePlaylistById(id: String){
        viewModelScope.launch(Dispatchers.IO) {
            playlistsState.postValue(PlaylistsUiState.LOADING)
            repository.deletePlaylistById(id)
            playlists.postValue(repository.getPlaylists())
            playlistsState.postValue(PlaylistsUiState.READY)
        }
    }

}