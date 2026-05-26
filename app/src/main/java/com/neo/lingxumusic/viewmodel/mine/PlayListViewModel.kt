package com.neo.lingxumusic.viewmodel.mine


import com.neo.lingxumusic.api.UserApi
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.model.PlaylistBrief
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayListViewModel @Inject constructor(private val userApi : UserApi)
    : BaseViewStateViewModel() {

    val songDetailResult = ViewStateMutableLiveData()

    fun getSongDetail(playlist: PlaylistBrief) {
        launch(songDetailResult) {
            userApi.getPlaylistSong(playlist.global_collection_id)
        }
    }

}