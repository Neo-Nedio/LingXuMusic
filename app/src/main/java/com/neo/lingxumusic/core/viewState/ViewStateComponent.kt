package com.neo.lingxumusic.core.viewState

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.gson.JsonParseException
import com.neo.lingxumusic.R
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener
import com.neo.lingxumusic.model.BaseResult
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Description->页面状态切换组件, 根据viewStateLiveData，自动切换各种状态页面
 * @param modifier：页面布局修饰
 * @param viewStateLiveData：页面状态livedata
 * @param refreshFlag：刷新标志，refreshFlag=0是，页面和各个页面状态绑定，refreshFlag>0时，页面只显示正常数据，并且refreshFlag变化时会自动刷新页面数据
 * @param lifeCycleListener：生命周期监听
 * @param loadDataBlock：数据加载
 * @param specialRetryBlock：特殊的重试请求代码块,没设置时重试逻辑将直接调用loadDataBlock
 * @param viewStateComponentModifier: 状态页面修饰
 * @param viewStateContentAlignment：状态页面居中方式
 * @param customEmptyComponent：自定义空布局,没设置则使用默认空布局
 * @param customFailComponent：自定义失败布局,没设置则使用默认失败布局
 * @param customErrorComponent：自定义错误布局,没设置则使用默认错误布局
 * @param contentView：正常页面内容
 */
@Composable
fun ViewStateComponent(
    modifier: Modifier = Modifier,
    viewStateLiveData: ViewStateLiveData?,
    refreshFlag: Int = 0,
    lifeCycleListener: ComposeLifeCycleListener? = null,
    loadDataBlock: (() -> Unit)? = null,
    specialRetryBlock: (() -> Unit)? = null,
    viewStateComponentModifier: Modifier = Modifier.fillMaxSize(),
    viewStateContentAlignment: Alignment = Alignment.Center,
    customEmptyComponent: @Composable (() -> Unit)? = null,
    customFailComponent: @Composable ((errorMessage: String?) -> Unit)? = null,
    customErrorComponent: @Composable ((errorMessage: Pair<String, Int>) -> Unit)? = null,
    contentView: @Composable BoxScope.(data: BaseResult) -> Unit
) {

    //生命周期监听
    //让 Compose 页面能够通过 ComposeLifeCycleListener 接口，感知 Activity/Fragment 的生命周期事件
    lifeCycleListener?.let { listener ->
        //获取当前 Compose 界面所依附的 Activity 或 Fragment
        val lifecycleOwner = LocalLifecycleOwner.current

        //Compose 提供的一个"副作用"函数，在 Compose UI 渲染之外做的操作，比如注册监听器、开启定时器、连接数据库
        //key为Unit,只会在组件首次进入屏幕时执行一次，不会重复执行
        DisposableEffect(Unit) {
            //告诉外部监听器：页面进入了 Composable 环境（即页面即将显示）
            listener.onEnterCompose(lifecycleOwner)

            val lifecycleEventObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        listener.onCreate(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_START -> {
                        listener.onStart(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        listener.onResume(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        listener.onPause(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_STOP -> {
                        listener.onStop(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        listener.onDestroy(lifecycleOwner)
                    }
                    Lifecycle.Event.ON_ANY -> Unit
                }
            }

            //注册观察者
            lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

            //注册清理代码
            onDispose {
                //通知外部：页面退出了 Compose
                listener.onExitCompose(lifecycleOwner)
                // 移除观察者，防止内存泄漏
                lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
            }
        }
    }

    //缓存成功数据
    val successData = remember {
        mutableStateOf<com.neo.lingxumusic.model.BaseResult?>(null)
    }


    if (viewStateLiveData != null) {
        //观察 LiveData
        val viewState by viewStateLiveData.observeAsState()

        //默认模式
        if (refreshFlag == 0) {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // 根据当前状态，显示不同的界面
                when (viewState) {
                    is ViewState.Loading -> {
                        LoadingComponent(
                            modifier = viewStateComponentModifier,
                            contentAlignment = viewStateContentAlignment
                        )
                    }
                    is ViewState.Success -> {
                        successData.value = (viewState as ViewState.Success).data
                        contentView(successData.value!!)
                    }
                    is ViewState.Empty -> {
                        // 优先使用外部自定义的页面，没有就用默认的
                        customEmptyComponent?.invoke() ?: NoSuccessComponent(
                            loadDataBlock = loadDataBlock,
                            contentAlignment = viewStateContentAlignment,
                            specialRetryBlock = specialRetryBlock,
                            modifier = viewStateComponentModifier
                        )
                    }
                    is ViewState.Fail -> {
                        // 优先使用外部自定义的页面，没有就用默认的
                        customFailComponent?.invoke((viewState as ViewState.Fail).errorMsg) ?: NoSuccessComponent(
                            modifier = viewStateComponentModifier,
                            message = "${(viewState as ViewState.Fail).errorMsg} 点我重试",
                            loadDataBlock = loadDataBlock,
                            specialRetryBlock = specialRetryBlock,
                            contentAlignment = viewStateContentAlignment
                        )
                    }
                    is ViewState.Error -> {
                        // 优先使用外部自定义的页面，没有就用默认的
                        if (customErrorComponent != null) {
                            customErrorComponent.invoke(getErrorMessagePair((viewState as ViewState.Error).exception))
                        } else {
                            val errorMessagePair = getErrorMessagePair((viewState as ViewState.Error).exception)
                            NoSuccessComponent(
                                modifier = viewStateComponentModifier,
                                message = errorMessagePair.first,
                                iconResId = errorMessagePair.second,
                                loadDataBlock = loadDataBlock,
                                specialRetryBlock = specialRetryBlock,
                                contentAlignment = viewStateContentAlignment,
                            )
                        }
                    }
                    else -> {
                        loadDataBlock?.invoke()
                    }
                }
            }
        } else {
            // refreshFlag变化时自动刷新数据
            LaunchedEffect(refreshFlag) {
                loadDataBlock?.invoke()
            }

            // 更新缓存数据
            if (viewState is ViewState.Success) {
                successData.value = (viewState as ViewState.Success).data
            }

            // 直接显示缓存的数据
            successData.value?.let {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    contentView(it)
                }
            }
        }
    }
}


//获取错误信息
fun getErrorMessagePair(exception: Throwable): Pair<String, Int> {
    return when (exception) {
        is ConnectException,
        is UnknownHostException -> {
            Pair("网络连接失败", R.drawable.ic_network_error)
        }
        is SocketTimeoutException -> {
            Pair("网络连接超时", R.drawable.ic_network_error)
        }
        is JsonParseException -> {
            Pair("数据解析错误", R.drawable.ic_network_error)
        }
        else -> {
            Pair("未知错误", R.drawable.ic_network_error)
        }
    }
}
