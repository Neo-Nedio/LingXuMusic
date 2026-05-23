package com.neo.lingxumusic.core

import com.neo.lingxumusic.model.LoginResult
import com.neo.lingxumusic.utils.kvCacheParcelable

//全局数据
object AppGlobalData {
    var sLoginResult by kvCacheParcelable(LoginResult::class.java)
}