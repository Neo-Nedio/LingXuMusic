package com.neo.lingxumusic.utils


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
}

// url定义替换函数
fun String.replaceSize(size: Int = 480): String {
    return replace("{size}", size.toString())
}