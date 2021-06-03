package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import com.arpadfodor.android.songquiz.model.repository.dataclasses.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlaylistsAddUiState{
    LOADING, READY, NOT_FOUND, ERROR_ADD_PLAYLIST, SUCCESS_ADD_PLAYLIST, PLAYLIST_ALREADY_ADDED
}

@HiltViewModel
class PlaylistsAddViewModel @Inject constructor(
    var repository: PlaylistsRepository
) : ViewModel() {

    companion object{
        var transferPlaylistIdsAlreadyAdded : List<String> = listOf()
    }

    val searchResult : MutableLiveData<SearchResult> by lazy {
        MutableLiveData<SearchResult>()
    }

    val playlistsAddState: MutableLiveData<PlaylistsAddUiState> by lazy {
        MutableLiveData<PlaylistsAddUiState>()
    }

    var playlistIdsAlreadyAdded = mutableListOf<String>()

    init {
        playlistsAddState.value = PlaylistsAddUiState.READY
    }

    fun setPlaylistIdsAlreadyAdded(){
        playlistIdsAlreadyAdded = transferPlaylistIdsAlreadyAdded.toMutableList()
    }

    fun searchPlaylistByIdOrName(searchExpression: String){

        viewModelScope.launch(Dispatchers.IO) {
            playlistsAddState.postValue(PlaylistsAddUiState.LOADING)

            val result = repository.searchPlaylistByIdOrName(searchExpression)
            searchResult.postValue(repository.searchPlaylistByIdOrName(searchExpression))

            if(result.items.isEmpty()){
                playlistsAddState.postValue(PlaylistsAddUiState.NOT_FOUND)
            }
            else{
                playlistsAddState.postValue(PlaylistsAddUiState.READY)
            }
        }

    }

    fun searchGetNextResult(){
        val currentResult = searchResult.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            playlistsAddState.postValue(PlaylistsAddUiState.LOADING)
            searchResult.postValue(repository.searchGetNextResult(currentResult))
            playlistsAddState.postValue(PlaylistsAddUiState.READY)
        }
    }

    fun addPlaylistById(id: String){

        if(playlistIdsAlreadyAdded.contains(id)){
            playlistsAddState.postValue(PlaylistsAddUiState.PLAYLIST_ALREADY_ADDED)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            playlistsAddState.postValue(PlaylistsAddUiState.LOADING)
            val success = searchResult.value?.items?.let { repository.addPlaylist(it.first{ item -> item.id == id }) } ?: false

            if(success){
                playlistsAddState.postValue(PlaylistsAddUiState.SUCCESS_ADD_PLAYLIST)
                playlistIdsAlreadyAdded.add(id)
            }
            else{
                playlistsAddState.postValue(PlaylistsAddUiState.ERROR_ADD_PLAYLIST)
            }
        }

    }

}