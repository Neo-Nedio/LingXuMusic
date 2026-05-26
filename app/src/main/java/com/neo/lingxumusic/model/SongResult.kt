package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 歌单详情数据
 */
@Parcelize
data class PlaylistDetailData(
    val begin_idx: Int = 0,                 // 起始索引
    val pagesize: Int = 30,                 // 每页大小
    val count: Int = 0,                     // 总歌曲数
    val userid: Long = 0,                   // 用户 ID
    val songs: List<Song>? = null,          // 歌曲列表
    val list_info: PlaylistInfo? = null     // 歌单信息
) : Parcelable

/**
 * 歌曲信息
 */
@Parcelize
data class Song(
    val hash: String? = null,               // 歌曲 hash（用于播放）
    val audio_id: Int = 0,                  // 音频 ID
    val name: String? = null,               // 歌曲名（含歌手）
    val singerinfo: List<SingerInfo>? = null, // 歌手信息列表
    val album_id: String? = null,           // 专辑 ID
    val albuminfo: AlbumInfo? = null,       // 专辑信息
    val cover: String? = null,              // 封面图片 URL
    val size: Int = 0,                      // 文件大小（字节）
    val timelen: Int = 0,                   // 时长（毫秒）
    val bitrate: Int = 0,                   // 比特率
    val extname: String? = null,            // 扩展名（如 mp3）
    val language: String? = null,           // 语言（如 国语）
    val publish_date: String? = null,       // 发行日期
    val collecttime: Long = 0,              // 收藏时间
    val privilege: Int = 0,                 // 权限
    val sort: Int = 0,                      // 排序
    val mvtype: Int = 0,                    // MV 类型
    val mvhash: String? = null,             // MV hash
    val mvtrack: Int = 0,                   // MV 轨道
    val remark: String? = null,             // 备注
    val level: Int = 0,                     // 音质等级
    val feetype: Int = 0,                   // 收费类型
    val rcflag: Int = 0,                    // 版权标志
    val has_obbligato: Int = 0,             // 是否有伴奏
    val bpm: Int = 0,                       // 节拍
    val bpm_type: String? = null,           // 节拍类型
    val add_mixsongid: Long = 0,            // 混合歌曲 ID
    val mixsongid: Long = 0,                // 混合歌曲 ID
    val audio_group_id: String? = null,     // 音频组 ID
    val fileid: Int = 0,                    // 文件 ID
    val heat: Int = 0,                      // 热度
    val shield: Int = 0,                    // 是否屏蔽（1=屏蔽）
    val trans_param: SongTransParam? = null, // 传输参数
    val relate_goods: List<RelateGood>? = null, // 相关音质
    val download: List<DownloadInfo>? = null // 下载信息
) : Parcelable

/**
 * 歌手信息
 */
@Parcelize
data class SingerInfo(
    val id: Int = 0,                        // 歌手 ID
    val name: String? = null,               // 歌手名
    val avatar: String? = null,             // 歌手头像
    val publish: Int = 1,                   // 是否发布
    val type: Int = 0                       // 类型
) : Parcelable

/**
 * 专辑信息
 */
@Parcelize
data class AlbumInfo(
    val id: Int = 0,                        // 专辑 ID
    val name: String? = null,               // 专辑名
    val publish: Int = 1                    // 是否发布
) : Parcelable

/**
 * 歌曲传输参数
 */
@Parcelize
data class SongTransParam(
    val language: String? = null,           // 语言
    val display: Int = 0,                   // 显示标志
    val display_rate: Int = 0,              // 显示速率
    val pay_block_tpl: Int = 0,             // 付费模板
    val cid: Int = 0,                       // 分类 ID
    val qualitymap: Map<String, Int>? = null, // 音质映射
    val classmap: Map<String, Long>? = null, // 分类映射
    val ipmap: Map<String, Long>? = null,   // IP 映射
    val cpy_attr0: Long = 0,                // 版权属性
    val musicpack_advance: Int = 0,         // 音乐包高级
    val is_original: Int = 0,               // 是否原创
    val union_cover: String? = null,        // 联合封面
    val songname_suffix: String? = null,    // 歌曲名后缀
    val ogg_128_hash: String? = null,       // OGG 128k hash
    val ogg_128_filesize: Int = 0,          // OGG 128k 文件大小
    val ogg_320_hash: String? = null,       // OGG 320k hash
    val ogg_320_filesize: Int = 0,          // OGG 320k 文件大小
    val hash_multitrack: String? = null,    // 多轨 hash
    val hash_offset: HashOffset? = null,    // hash 偏移
    val appid_block: String? = null,        // App ID 块
    val cpy_level: Int = 0,                 // 版权等级
    val cpy_grade: Int = 0                  // 版权等级
) : Parcelable

/**
 * Hash 偏移信息
 */
@Parcelize
data class HashOffset(
    val start_byte: Int = 0,                // 起始字节
    val end_byte: Int = 0,                  // 结束字节
    val start_ms: Int = 0,                  // 起始毫秒
    val end_ms: Int = 0,                    // 结束毫秒
    val offset_hash: String? = null,        // 偏移 hash
    val clip_hash: String? = null,          // 片段 hash
    val file_type: Int = 0                  // 文件类型
) : Parcelable

/**
 * 相关音质
 */
@Parcelize
data class RelateGood(
    val size: Int = 0,                      // 文件大小
    val hash: String? = null,               // hash
    val level: Int = 0,                     // 音质等级（2=128k, 4=320k, 5=无损）
    val privilege: Int = 0,                 // 权限
    val bitrate: Int = 0                    // 比特率
) : Parcelable

/**
 * 下载信息
 */
@Parcelize
data class DownloadInfo(
    val status: Int = 0,                    // 状态
    val hash: String? = null,               // hash
    val fail_process: Int = 0,              // 失败处理
    val pay_type: Int = 0                   // 付费类型
) : Parcelable

/**
 * 歌单信息（详情）
 */
@Parcelize
data class PlaylistInfo(
    val listid: Int = 0,                    // 歌单 ID
    val name: String? = null,               // 歌单名称
    val pic: String? = null,                // 封面图片 URL
    val intro: String? = null,              // 简介
    val tags: String? = null,               // 标签（逗号分隔）
    val count: Int = 0,                     // 歌曲数量
    val create_time: Long = 0,              // 创建时间
    val update_time: Long = 0,              // 更新时间
    val publish_date: String? = null,       // 发布日期
    val list_create_username: String? = null, // 创建者昵称
    val list_create_userid: Long = 0,       // 创建者 ID
    val create_user_pic: String? = null,    // 创建者头像
    val create_user_gender: Int = 0,        // 创建者性别
    val source: Int = 0,                    // 来源（1=用户创建,2=收藏）
    val type: Int = 0,                      // 类型
    val status: Int = 0,                    // 状态（1=正常）
    val code: Int = 0,                      // 状态码（1=正常）
    val is_pri: Int = 0,                    // 是否私有
    val is_per: Int = 0,                    // 是否个人
    val is_mine: Int = 0,                   // 是否我的
    val is_publish: Int = 0,                // 是否发布
    val is_drop: Int = 0,                   // 是否丢弃
    val is_edit: Int = 0,                   // 是否可编辑
    val is_featured: Int = 0,               // 是否精选
    val is_custom_pic: Int = 0,             // 是否自定义封面
    val is_def: Int = 0,                    // 是否默认
    val collect_total: Int = 0,             // 收藏总数
    val heat: Int = 0,                      // 热度
    val kq_talent: Int = 0,                 // 酷狗才艺值
    val list_ver: Int = 0,                  // 版本号
    val per_num: Int = 0,                   // 权限数
    val per_count: Int = 0,                 // 权限计数
    val sort: Int = 0,                      // 排序
    val pub_type: Int = 0,                  // 发布类型
    val pub_new: Int = 0,                   // 是否新发布
    val sound_quality: String? = null,      // 音质
    val radio_id: Int = 0,                  // 电台 ID
    val musiclib_id: Int = 0,               // 音乐库 ID
    val list_create_listid: Int = 0,        // 创建者歌单 ID
    val global_collection_id: String? = null, // 全局收藏 ID
    val parent_global_collection_id: String? = null, // 父级全局收藏 ID
    val list_create_gid: String? = null,    // 创建者全局 ID
    val musiclib_tags: List<MusicTag>? = null, // 标签列表
    val trans_param: TransParam? = null,    // 转换参数
) : Parcelable

