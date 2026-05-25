package com.neo.lingxumusic.core.viewState

import com.neo.lingxumusic.model.BaseResult

//来表示 UI 的不同状态
sealed class ViewState {
    object Loading : ViewState()
    data class Success(val data: BaseResult) : ViewState()
    object Empty : ViewState()
    data class Fail(val errorCode: String, val errorMsg: String) : ViewState()
    data class Error(val exception: Throwable) : ViewState()
}