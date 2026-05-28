package com.neo.lingxumusic.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.neo.lingxumusic.core.viewState.listener.ComposeLifeCycleListener

@Composable
fun LifeCycleObserverComponent(
    lifeCycleListener: ComposeLifeCycleListener? = null,  // 生命周期监听器
    contentView: @Composable () -> Unit                    // 内容 UI
) {
    val lifecycleOwner = LocalLifecycleOwner.current  // 获取当前的生命周期所有者（Activity/Fragment）

    // 只有传入了监听器才注册
    if (lifeCycleListener != null) {
        DisposableEffect(lifecycleOwner, lifeCycleListener) {
            // 1. 进入 Compose 时回调
            lifeCycleListener.onEnterCompose(lifecycleOwner)

            // 2. 创建生命周期观察者
            val lifecycleEventObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> lifeCycleListener.onCreate(lifecycleOwner)
                    Lifecycle.Event.ON_START -> lifeCycleListener.onStart(lifecycleOwner)
                    Lifecycle.Event.ON_RESUME -> lifeCycleListener.onResume(lifecycleOwner)
                    Lifecycle.Event.ON_PAUSE -> lifeCycleListener.onPause(lifecycleOwner)
                    Lifecycle.Event.ON_STOP -> lifeCycleListener.onStop(lifecycleOwner)
                    Lifecycle.Event.ON_DESTROY -> lifeCycleListener.onDestroy(lifecycleOwner)
                    Lifecycle.Event.ON_ANY -> Unit
                }
            }

            // 3. 注册观察者
            lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

            // 4. 组件销毁时清理
            onDispose {
                lifeCycleListener.onExitCompose(lifecycleOwner)
                lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
            }
        }
    }

    // 渲染内容
    contentView()
}
