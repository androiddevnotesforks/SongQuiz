package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    var repository: PlaylistsRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is playlist Fragment"
    }
    val text: LiveData<String> = _text

    val loadingState: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun addPlaylistById(id: String){
        viewModelScope.launch(Dispatchers.IO) {
            loadingState.postValue(true)
            repository.addPlaylistById(id)
            loadingState.postValue(false)
        }
    }

}