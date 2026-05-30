package com.neo.lingxumusic.utils

import java.util.Locale


object StringUtil {

    fun friendlyNumber(num: Number): String {
        if(num.toLong() < 10000) {
            return num.toString()
        }else if(num.toLong() < 100000000) {
            val result = num.toLong() / 10000
            return result.toString() + "万"
        }else if(num.toLong() >= 100000000) {
            val result = num.toLong() / 100000000
            return result.toString() + "亿"
        }
        return num.toString()
    }

    /**
     * 解析歌曲名字符串，提取歌名和歌手名
     * 格式： "洛天依、乐正绫、言和 - 酆都冥司记"
     * 结果： 歌手 = "洛天依、乐正绫、言和"，歌名 = "酆都冥司记"
     */
    fun parseSongName(fullName: String): Pair<String, String> {
        val separatorIndex = fullName.indexOf(" - ")
        return if (separatorIndex != -1) {
            val singer = fullName.substring(0, separatorIndex)
            val songName = fullName.substring(separatorIndex + 3)
            Pair(singer, songName)
        } else {
            // 如果没有分隔符，歌手设为空或 "未知"
            Pair("", fullName)
        }
    }

    //格式化时间
    fun formatMilliseconds(milliseconds: Int): String {
        val standardTime: String
        val seconds = milliseconds / 1000
        if (seconds <= 0) {
            standardTime = "00:00"
        } else if (seconds < 60) {
            standardTime = String.format(Locale.getDefault(), "00:%02d", seconds % 60)
        } else if (seconds < 3600) {
            standardTime = java.lang.String.format(
                Locale.getDefault(),
                "%02d:%02d",
                seconds / 60,
                seconds % 60
            )
        } else {
            standardTime = String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                seconds / 3600,
                seconds % 3600 / 60,
                seconds % 60
            )
        }
        return standardTime
    }
}

// url定义替换函数
fun String.replaceSize(size: Int = 480): String {
    return replace("{size}", size.toString())
}

/**
 * 解析回复内容，提取用户回复和被引用的用户名
 * 格式：回复内容 //@用户名: 被引用的内容
 * @return Triple(用户回复内容, 被引用用户名, 被引用内容)
 */
fun String.parseReply(): Triple<String, String?, String?> {
    val startIndex = this.indexOf("//@")
    if (startIndex == -1) return Triple(this, null, null)

    // 用户回复内容 = //@ 之前的部分
    val replyText = this.substring(0, startIndex).trim()

    // 从 //@ 之后开始找第一个 : 或 ：
    val afterAt = this.substring(startIndex + 3)
    val colonIndex = afterAt.indexOfFirst { it == ':' || it == '：' }
    if (colonIndex == -1) return Triple(this, null, null)

    // 用户名 = //@ 和 冒号 之间的部分
    val userName = afterAt.substring(0, colonIndex).trim()

    // 被引用的内容 = 冒号之后的部分
    val quotedContent = afterAt.substring(colonIndex + 1).trim()

    return Triple(replyText, userName, quotedContent)
}