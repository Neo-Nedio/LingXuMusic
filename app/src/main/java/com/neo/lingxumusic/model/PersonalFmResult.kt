package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PersonalFmData(
    val song_list: List<RecommendSong>? = null,  // ✅ 复用之前的 RecommendSong
    val mode: String? = null,                     // 模式：normal/small/peak
    val algorithm_id: Int = 0,
    val bi_biz: String? = null,
    val hotsong_num: Int = 0,
    val is_clean: Int = 0,
    val fresh_mode: Int = 0,
    val cur_mark: Long = 0
) : Parcelable