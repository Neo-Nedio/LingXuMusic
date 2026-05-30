package com.neo.lingxumusic.core.viewState

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.neo.lingxumusic.model.BaseResult
import kotlinx.coroutines.launch
import retrofit2.HttpException

//类型别名
typealias ViewStateMutableLiveData<T> = MutableLiveData<ViewState<T>>
typealias ViewStateLiveData<T> = LiveData<ViewState<T>>

open class BaseViewStateViewModel : ViewModel() {

    protected fun <T : BaseResult> launch(
        liveData: ViewStateMutableLiveData<T>? = null,          // 要更新的 LiveData
        handleResult: ((T) -> Unit)? = null,// 成功后的额外处理（可选）
        judgeEmpty: ((T) -> Boolean)? = null,// 判断是否为空数据（可选）
        call: suspend () -> T               // 实际的网络请求
    ) {
        viewModelScope.launch {
            runCatching { // 捕获异常，不会崩溃
                liveData?.let {
                    it.value = ViewState.Loading //  Loading 状态
                }
                call()                  // 执行网络请求
            }.onSuccess { result ->     // 请求成功（没抛异常）
                if (result.status == 1 && result.error_code == 0) {  // 业务成功
                    if (judgeEmpty?.invoke(result) == true) {        // 判断是否空数据
                        liveData?.let {
                            it.value = ViewState.Empty // 发 Empty
                        }
                    } else {
                        handleResult?.invoke(result)                 // 执行回调（如保存数据）
                        liveData?.let {
                            it.value = ViewState.Success(result)  // 发 Success，带数据
                        }
                    }
                } else { // 业务失败
                    liveData?.let {
                        it.value = ViewState.Fail(result.status.toString(), result.data)
                    }
                }
            }.onFailure { e -> // 网络异常、解析异常等
                liveData?.let {
                    it.value = e.toViewState()
                }
            }
        }
    }

    private fun Throwable.toViewState(): ViewState<Nothing> {
        if (this !is HttpException) {
            return ViewState.Error(this)
        }

        val errorBody = response()?.errorBody()?.string().orEmpty()
        val result = runCatching { Gson().fromJson(errorBody, BaseResult::class.java) }.getOrNull()
        return if (result?.data != null) {
            ViewState.Fail(code().toString(), result.data)
        } else {
            ViewState.Error(this)
        }
    }
}