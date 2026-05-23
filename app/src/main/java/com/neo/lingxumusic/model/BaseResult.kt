package com.neo.lingxumusic.model

import java.io.Serializable

open class BaseResult(
    val status: Int = 0,        // 1=成功
    val error_code: Int = 0     // 0=无错误
) : Serializable