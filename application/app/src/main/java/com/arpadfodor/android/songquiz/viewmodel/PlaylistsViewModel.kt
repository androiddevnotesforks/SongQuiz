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
    LOADING, READY, SHOW_ADD_SCREEN, CANNOT_FIND_PLAYLIST
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

    fun searchPlaylistsByIdOrName(searchExpression: String){
        viewModelScope.launch(Dispatchers.IO) {

            playlistsState.postValue(PlaylistsUiState.LOADING)
            val playlistsFound = repository.searchPlaylistsByIdOrName(searchExpression)
            val playlistIdsAlreadyAdded : List<String> = playlists.value?.map { it -> it.id } ?: listOf()

            if(playlistsFound.isNotEmpty()){
                // pass playlists found and playlist ids already added to add view model in a companion
                PlaylistsAddViewModel.transferPlaylistsToShow = playlistsFound
                PlaylistsAddViewModel.transferPlaylistIdsAdded = playlistIdsAlreadyAdded
                // show playlist add view
                playlistsState.postValue(PlaylistsUiState.SHOW_ADD_SCREEN)
            }
            else{
                playlistsState.postValue(PlaylistsUiState.CANNOT_FIND_PLAYLIST)
            }

        }
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