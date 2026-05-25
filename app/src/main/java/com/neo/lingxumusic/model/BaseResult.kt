package com.neo.lingxumusic.model

import com.google.gson.Gson
import com.google.gson.JsonElement
import java.io.Serializable

open class BaseResult(
    val status: Int = 0,        // 1=成功
    val error_code: Int = 0,    // 0=无错误
    val data: JsonElement? = null
) : Serializable

inline fun <reified T> BaseResult.dataAs(): T? {
    return data?.let { Gson().fromJson(it, T::class.java) }
}