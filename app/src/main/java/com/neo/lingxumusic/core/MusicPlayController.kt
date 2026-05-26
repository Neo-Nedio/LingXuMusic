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