package com.neo.lingxumusic.core.viewState

import com.google.gson.JsonElement

//来表示 UI 的不同状态
sealed class ViewState<out T> {
    object Loading : ViewState<Nothing>()
    data class Success<T>(val result: T) : ViewState<T>()
    object Empty : ViewState<Nothing>()
    data class Fail(val errorCode: String, val errorMsg: JsonElement?) : ViewState<Nothing>()
    data class Error(val exception: Throwable) : ViewState<Nothing>()
}