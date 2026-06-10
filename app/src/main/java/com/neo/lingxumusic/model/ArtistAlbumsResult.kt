package com.neo.lingxumusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 专辑信息
 */
@Parcelize
data class ArtistAlbum(
    val album_id: Long = 0,
    val album_name: String? = null,
    val author_name: String? = null,
    val type: String? = null,
    val sizable_cover: String? = null,
    val cover: String? = null,
    val authors: List<AlbumAuthor>? = null,
    val heat: Int = 0,
    val publish_date: String? = null,
    val intro: String? = null,
    val language: String? = null,
    val publish_company: String? = null,
    val is_publish: Int = 0,
    val grade: Int = 0,
    val grade_count: Int = 0,
    val quality: Int = 0,
    val exclusive: Int = 0,
    val sum_ownercount: Int = 0,
    val language_id: Int = 0,
    val category: Int = 0,
    val special_tag: String? = null,
    val goods_info: AlbumGoodsInfo? = null,
    val privilege_download: AlbumPrivilegeDownload? = null
) : Parcelable

/**
 * 专辑作者
 */
@Parcelize
data class AlbumAuthor(
    val author_id: Long = 0,
    val author_name: String? = null
) : Parcelable

/**
 * 专辑商品信息
 */
@Parcelize
data class AlbumGoodsInfo(
    val could_buy_album: String? = null,
    val album_pay_type: Int = 0,
    val album_sale_url: String? = null,
    val album_price: Int = 0
) : Parcelable

/**
 * 专辑下载权限
 */
@Parcelize
data class AlbumPrivilegeDownload(
    val privilege: Int = 0,
    val fail_process: Int = 0
) : Parcelable