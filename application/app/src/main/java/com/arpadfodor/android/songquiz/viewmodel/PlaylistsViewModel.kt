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

enum class PlaylistsState{
    ERROR_PLAYLIST_ADD, LOADING, READY
}

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    var repository: PlaylistsRepository
) : ViewModel() {

    val playlists : MutableLiveData<List<Playlist>> by lazy {
        MutableLiveData<List<Playlist>>()
    }

    val playlistsState: MutableLiveData<PlaylistsState> by lazy {
        MutableLiveData<PlaylistsState>()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            playlistsState.postValue(PlaylistsState.LOADING)
            playlists.postValue(repository.getPlaylists().reversed())
            playlistsState.postValue(PlaylistsState.READY)
        }
    }

    fun addPlaylistById(id: String){
        viewModelScope.launch(Dispatchers.IO) {

            playlistsState.postValue(PlaylistsState.LOADING)
            val success = repository.addPlaylistById(id)

            if(success){
                playlists.postValue(repository.getPlaylists())
                playlistsState.postValue(PlaylistsState.READY)
            }
            else{
                playlistsState.postValue(PlaylistsState.ERROR_PLAYLIST_ADD)
            }

        }
    }

    fun deletePlaylistById(id: String){
        viewModelScope.launch(Dispatchers.IO) {
            playlistsState.postValue(PlaylistsState.LOADING)
            repository.deletePlaylistById(id)
            playlists.postValue(repository.getPlaylists())
            playlistsState.postValue(PlaylistsState.READY)
        }
    }

}