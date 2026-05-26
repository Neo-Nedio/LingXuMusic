package com.neo.lingxumusic.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.model.Song


object MusicPlayController {
    var songList = mutableStateListOf<Song>()

    var curIndex by mutableStateOf(0)

    private var playing by mutableStateOf(false)

    //用于排除无法播放的歌曲
    fun setSongListAndIndex(songs: List<Song>, hash: String?){
        // 1. 过滤出有 hash 的歌曲
        val validSongs = songs.filter { !it.hash.isNullOrEmpty() }

        // 2. 赋值给 songList
        songList.addAll(validSongs)

        // 3. 根据传入的 hash 查找 curIndex
        curIndex = if (hash != null) {
            validSongs.indexOfFirst { it.hash == hash }.coerceAtLeast(0)
        } else {
            0
        }
    }

    fun play() {
        playing = true
    }

    fun pause() {
        playing = false
    }

    fun isPlaying(): Boolean {
        return playing
    }


}