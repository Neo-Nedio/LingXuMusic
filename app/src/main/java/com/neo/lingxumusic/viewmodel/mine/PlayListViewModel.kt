package com.neo.lingxumusic.viewmodel.mine


import com.neo.lingxumusic.http.api.UserApi
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.model.PlaylistBrief
import com.neo.lingxumusic.model.PlaylistDetailData
import com.neo.lingxumusic.model.Song
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.collections.orEmpty

@HiltViewModel
class PlayListViewModel @Inject constructor(private val userApi : UserApi)
    : BaseViewStateViewModel() {

    val songDetailResult = ViewStateMutableLiveData()

    val songList = mutableListOf<Song>()

    fun getSongDetail(playlist: PlaylistBrief) {
        launch(songDetailResult, handleResult = {
            val detail = it.dataAs<PlaylistDetailData>()
            val songs = detail?.songs.orEmpty()
            songList.addAll(songs)
        }) {
            userApi.getPlaylistSong(playlist.global_collection_id)
        }
    }

}