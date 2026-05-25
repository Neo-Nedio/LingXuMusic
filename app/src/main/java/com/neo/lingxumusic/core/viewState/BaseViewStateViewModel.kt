package com.neo.lingxumusic.core.viewState

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neo.lingxumusic.model.BaseResult
import kotlinx.coroutines.launch

//类型别名
typealias ViewStateMutableLiveData = MutableLiveData<ViewState>
typealias ViewStateLiveData = LiveData<ViewState>

open class BaseViewStateViewModel : ViewModel() {

    protected fun launch(
        liveData: ViewStateMutableLiveData,          // 要更新的 LiveData
        handleResult: ((BaseResult) -> Unit)? = null,// 成功后的额外处理（可选）
        judgeEmpty: ((BaseResult) -> Boolean)? = null,// 判断是否为空数据（可选）
        call: suspend () -> BaseResult               // 实际的网络请求
    ) {
        viewModelScope.launch {
            runCatching { // 捕获异常，不会崩溃
                liveData.value = ViewState.Loading  // 发 Loading 状态
                call()                  // 执行网络请求
            }.onSuccess { result ->     // 请求成功（没抛异常）
                if (result.status == 1 && result.error_code == 0) {  // 业务成功
                    if (judgeEmpty?.invoke(result) == true) {        // 判断是否空数据
                        liveData.value = ViewState.Empty             // 发 Empty
                    } else {
                        handleResult?.invoke(result)                 // 执行回调（如保存数据）
                        liveData.value = ViewState.Success(result)   // 发 Success，带数据
                    }
                } else { // 业务失败
                    liveData.value = ViewState.Fail(result.status.toString(), result.data.toString())
                }
            }.onFailure { e -> // 网络异常、解析异常等
                liveData.value = ViewState.Error(e)
            }
        }
    }
}