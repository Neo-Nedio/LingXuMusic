package com.neo.lingxumusic.utils

import android.text.TextUtils
import android.text.format.DateUtils
import com.neo.lingxumusic.model.LyricResult
import com.neo.lingxumusic.viewmodel.playMusic.LyricModel
import com.neo.lingxumusic.viewmodel.playMusic.LyricWordModel
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

object LyricUtil {
    /** LRC 行：[00:17.65]歌词内容 */
    private val PATTERN_LINE = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d{2,3}\\])+)(.+)")
    private val PATTERN_TIME = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]")

    /** KRC 行：[开始毫秒,持续毫秒]<偏移,持续,0>字... */
    private val PATTERN_KRC_LINE = Pattern.compile("\\[(\\d+),(\\d+)\\](.*)")
    private val PATTERN_KRC_WORD = Pattern.compile("<(\\d+),(\\d+),\\d+>([^<]*)")

    fun parse(lyricResult: LyricResult): List<LyricModel> {
        val content = lyricResult.decodeContent?.let(::stripBom) ?: return emptyList()
        if (TextUtils.isEmpty(content)) return emptyList()

        return when (lyricResult.fmt?.lowercase(Locale.ROOT)) {
            "lrc" -> parseLrc(content)
            "krc" -> parseKrc(content)
            else -> parseKrc(content).ifEmpty { parseLrc(content) }
        }.sortedBy { it.time }
    }

    private fun stripBom(text: String): String =
        if (text.startsWith("\uFEFF")) text.removePrefix("\uFEFF") else text

    /**
     * 解析 KRC 逐字歌词
     * 示例：[1247,3738]<0,209,0>致<209,188,0>命<397,281,0>的...
     */
    private fun parseKrc(krcText: String): List<LyricModel> {
        val entryList = ArrayList<LyricModel>()
        krcText.split("\r?\n".toRegex()).forEach { line ->
            parseKrcLine(line.trim())?.let { entryList.add(it) }
        }
        return entryList
    }

    private fun parseKrcLine(line: String): LyricModel? {
        if (TextUtils.isEmpty(line)) return null

        val lineMatcher = PATTERN_KRC_LINE.matcher(line)
        if (!lineMatcher.matches()) return null

        val lineStart = lineMatcher.group(1)!!.toLong()
        val lineDuration = lineMatcher.group(2)!!.toLong()
        val wordPart = lineMatcher.group(3) ?: return null

        val words = ArrayList<LyricWordModel>()
        val textBuilder = StringBuilder()
        val wordMatcher = PATTERN_KRC_WORD.matcher(wordPart)
        while (wordMatcher.find()) {
            val offset = wordMatcher.group(1)!!.toLong()
            val duration = wordMatcher.group(2)!!.toInt()
            val text = wordMatcher.group(3) ?: ""
            textBuilder.append(text)
            words.add(
                LyricWordModel(
                    text = text,
                    startTime = lineStart + offset,
                    duration = duration,
                )
            )
        }
        if (words.isEmpty()) return null

        return LyricModel(
            time = lineStart,
            lyric = textBuilder.toString(),
            duration = lineDuration,
            words = words,
        )
    }

    /**
     * 解析 LRC 整行歌词
     */
    private fun parseLrc(lrcText: String): List<LyricModel> {
        val entryList = ArrayList<LyricModel>()
        lrcText.split("\r?\n".toRegex()).forEach { line ->
            parseLrcLine(line)?.let { entryList.addAll(it) }
        }
        return entryList
    }

    private fun parseLrcLine(line: String): List<LyricModel>? {
        if (TextUtils.isEmpty(line)) return null

        val trimmed = line.trim()
        val lineMatcher: Matcher = PATTERN_LINE.matcher(trimmed)
        if (!lineMatcher.matches()) return null

        val times = lineMatcher.group(1) ?: return null
        val text = lineMatcher.group(3) ?: return null
        val entryList = ArrayList<LyricModel>()

        val timeMatcher: Matcher = PATTERN_TIME.matcher(times)
        while (timeMatcher.find()) {
            val min = timeMatcher.group(1)!!.toLong()
            val sec = timeMatcher.group(2)!!.toLong()
            val milString = timeMatcher.group(3)!!
            var mil = milString.toLong()
            if (milString.length == 2) {
                mil *= 10
            }
            val time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil
            entryList.add(LyricModel(time = time, lyric = text))
        }
        return entryList
    }

    /** 转为 [分:秒] */
    fun formatTime(milli: Long): String {
        val m = (milli / DateUtils.MINUTE_IN_MILLIS).toInt()
        val s = (milli / DateUtils.SECOND_IN_MILLIS % 60).toInt()
        val mm = String.format(Locale.getDefault(), "%02d", m)
        val ss = String.format(Locale.getDefault(), "%02d", s)
        return "$mm:$ss"
    }
}
