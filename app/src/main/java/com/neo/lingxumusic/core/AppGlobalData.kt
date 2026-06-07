package com.neo.lingxumusic.core

import com.neo.lingxumusic.core.player.PlayMode
import com.neo.lingxumusic.model.LoginData
import com.neo.lingxumusic.model.UserDetail
import com.neo.lingxumusic.utils.kvCache
import com.neo.lingxumusic.utils.kvCacheParcelable

//全局数据
object AppGlobalData {
    //登录信息
    var sLoginData: LoginData? by kvCacheParcelable(LoginData::class.java)

    val token: String
        get() = sLoginData?.token ?: ""

    val userId: Long
        get() = sLoginData?.userid ?: 0

    //用户详细信息
    var userDetail: UserDetail? by kvCacheParcelable(UserDetail::class.java)

    //播放模式
    private var playModeName by kvCache(PlayMode.LOOP.name)

    var playMode: PlayMode
        get() = runCatching { PlayMode.valueOf(playModeName) }.getOrDefault(PlayMode.LOOP)
        set(value) {
            playModeName = value.name
        }

    // 喜欢的音乐歌单的两个id
    var favoritePlaylistGlobalCollectionId by kvCache("")
    var favoritePlaylistListId by kvCache(0)

}