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

enum class PlaylistsAddUiState{
    LOADING, READY, ERROR_ADD_PLAYLIST, SUCCESS_ADD_PLAYLIST, PLAYLIST_ALREADY_ADDED
}

@HiltViewModel
class PlaylistsAddViewModel @Inject constructor(
    var repository: PlaylistsRepository
) : ViewModel() {

    companion object{
        var transferPlaylistsToShow : List<Playlist> = listOf()
        var transferPlaylistIdsAdded : List<String> = listOf()
    }

    val playlistsFound : MutableLiveData<List<Playlist>> by lazy {
        MutableLiveData<List<Playlist>>()
    }

    val playlistsAddState: MutableLiveData<PlaylistsAddUiState> by lazy {
        MutableLiveData<PlaylistsAddUiState>()
    }

    var playlistIdsAdded = mutableListOf<String>()

    init {
        playlistsAddState.value = PlaylistsAddUiState.READY
    }

    fun setPlaylistsFromCompanion(){
        playlistsFound.value = transferPlaylistsToShow
        playlistIdsAdded = transferPlaylistIdsAdded.toMutableList()
    }

    fun addPlaylistById(id: String){

        if(playlistIdsAdded.contains(id)){
            playlistsAddState.postValue(PlaylistsAddUiState.PLAYLIST_ALREADY_ADDED)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {

            playlistsAddState.postValue(PlaylistsAddUiState.LOADING)
            val success = playlistsFound.value?.let { repository.addPlaylist(it.first{ item -> item.id == id }) } ?: false

            if(success){
                playlistsAddState.postValue(PlaylistsAddUiState.SUCCESS_ADD_PLAYLIST)
                playlistIdsAdded.add(id)
            }
            else{
                playlistsAddState.postValue(PlaylistsAddUiState.ERROR_ADD_PLAYLIST)
            }

        }
    }

}