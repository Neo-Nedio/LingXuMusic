package com.neo.lingxumusic.core

import com.neo.lingxumusic.model.LoginData
import com.neo.lingxumusic.utils.kvCacheParcelable

//全局数据
object AppGlobalData {
    var sLoginData: LoginData? by kvCacheParcelable(LoginData::class.java)

    val token: String
        get() = sLoginData?.token ?: ""

    val userId: Long
        get() = sLoginData?.userid ?: 0

}