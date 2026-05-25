package com.neo.lingxumusic.core.viewState

import com.google.gson.JsonElement
import com.neo.lingxumusic.model.BaseResult

//来表示 UI 的不同状态
sealed class ViewState {
    object Loading : ViewState()
    data class Success(val result: BaseResult) : ViewState()
    object Empty : ViewState()
    data class Fail(val errorCode: String, val errorMsg: JsonElement?) : ViewState()
    data class Error(val exception: Throwable) : ViewState()
}