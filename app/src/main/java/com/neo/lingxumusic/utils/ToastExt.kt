package com.neo.lingxumusic.utils

import android.annotation.SuppressLint
import android.os.Looper
import android.widget.Toast
import com.neo.lingxumusic.core.LingxuApplication

private var toast: Toast? = null           // 全局单例 Toast 对象

/**
 * 弹出Toast信息。如果不是在主线程中调用此方法，Toast信息将会不显示。
 *
 * @param content
 * Toast中显示的内容
 */
@SuppressLint("ShowToast") //忽略 Android Studio 关于 Toast 未设置显示的警告
//让 Kotlin 函数有默认参数时，在 Java 中调用会生成多个重载方法
@JvmOverloads
fun showToast(content: String, duration: Int = Toast.LENGTH_SHORT) {
    if (Looper.myLooper() == Looper.getMainLooper()) { // 主线程才执行
        if (toast == null) {
            toast = Toast.makeText(LingxuApplication.getAppContext(), content, duration) // 第一次，创建新 Toast
        } else {
            toast?.setText(content) // 之后，只修改文本，不重新创建
        }
        toast?.show()
    }
}
