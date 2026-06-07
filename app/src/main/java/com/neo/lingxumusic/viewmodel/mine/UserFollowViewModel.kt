package com.neo.lingxumusic.viewmodel.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.http.api.UserApi
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.UserFollow
import com.neo.lingxumusic.model.UserFollowData
import com.neo.lingxumusic.model.dataAs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserFollowViewModel @Inject constructor(
    private val api: UserApi,
) : BaseViewStateViewModel() {

    // 关注列表
    var followList by mutableStateOf<List<UserFollow>>(emptyList())

    // 请求状态
    val userFollowResult = ViewStateMutableLiveData<BaseResult>()

    // 获取用户关注列表
    fun getUserFollow() {
        launch(userFollowResult, handleResult = {
            followList = it.dataAs<UserFollowData>()?.lists.orEmpty()
        }) {
            api.getUserFollow()
        }
    }
}
