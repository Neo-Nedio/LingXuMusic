package com.neo.lingxumusic.utils

import android.text.TextUtils
import android.text.format.DateUtils
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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

    //计算渐变终点
    @Composable
    fun animatedBrushEndFraction(
        words: List<LyricWordModel>,
        lineStartTime: Long,
        playPosition: Int,
    ): Float {
        //累加每个字的长度
        val totalChars = words.sumOf { it.text.length }.coerceAtLeast(1)
        var fraction = 0f
        for (word in words) {
            // 计算每个字的权重(字的长度占总字数的比例)
            val weight = word.text.length.toFloat() / totalChars
            //权重 * 动画值(这样当一个字的动画值到1后，只是根据权重沾满相应的字)
            fraction += weight * animatedWordProgress(word, lineStartTime, playPosition)
        }
        //限制返回值范围
        return fraction.coerceIn(0f, 1f)
    }

    /**
     * 单字：delay = startTime - 整句 startTime，duration = 该字 duration。
     * 行切入时用当前 playPosition 校准，避免 UI 切换晚于音频导致字幕滞后。
     */
    @Composable
    private fun animatedWordProgress(
        word: LyricWordModel, //单个字的数据（文字、开始时间、持续时间）
        lineStartTime: Long, //整行歌词的开始时间（毫秒）
        playPosition: Int, //当前播放位置（毫秒）
    ): Float {
        // 动画进度值，每个字独立，当字开始时间变化时重新创建
        val progress = remember(word.startTime) { Animatable(0f) }
        // 锁定进入当前行时的播放位置，避免因 playPosition 持续更新而反复触发动画
        //用户可能不是在整行开始的瞬间进入，可能是在播放过程中才打开歌词页面。entryPosition 锁定了进入时刻，以此作为动画时间轴的起点，确保动画与当前播放位置同步
        val entryPosition = remember { mutableIntStateOf(playPosition) }
        // 该字的结束时间 = 开始时间 + 持续时间
        val endTime = word.startTime + word.duration

        //切换到不同行、或同一行的不同字时 ， 切换到不同行时    执行
        LaunchedEffect(word.startTime, lineStartTime) {
            //当前播放位置
            val anchor = entryPosition.intValue
            when {
                // 情况1：已经错过了这个字
                // 条件：进入时的位置 ≥ 该字的结束时间
                // 处理：直接跳到 100% 完成，无动画
                anchor >= endTime -> progress.snapTo(1f)
                // 情况2：在该字的播放区间中间进入
                // 条件：进入时的位置在该字的时间范围内
                // 处理：从当前进度开始，动画到 100%
                anchor >= word.startTime -> {
                    // 计算当前进度（0f 到 1f）
                    val start = ((anchor - word.startTime).toFloat() / word.duration)
                        .coerceIn(0f, 1f)
                    progress.snapTo(start)// 立即跳转到当前进度
                    //执行动画到对应位置
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            // 剩余时间
                            durationMillis = (endTime - anchor).toInt().coerceAtLeast(1),
                            easing = LinearEasing,
                        ),
                    )
                }
                // 情况3：正常播放（还没到这个字）
                // 条件：进入时的位置在该字开始时间之前
                // 处理：延迟等待后，从 0% 动画到 100%
                else -> {
                    progress.snapTo(0f) // 起始进度为 0
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            // 字的持续时间
                            durationMillis = word.duration.coerceAtLeast(1),
                            // 等待时间
                            delayMillis = (word.startTime - anchor).toInt().coerceAtLeast(0),
                            easing = LinearEasing,
                        ),
                    )
                }
            }
        }
        return progress.value
    }
}
