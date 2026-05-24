package com.neo.lingxumusic.utils

import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified R, reified T> R.kvCache(defaultValue: T) =
    KVCacheExt("", defaultValue, T::class.java)

inline fun <reified R, reified T : Parcelable?> R.kvCacheParcelable(defaultValueRawType: Class<T>) =
    KVCacheParcelableExt("", defaultValueRawType)

object KVCache {
    //高性能键值对（Key-Value）存储组件
    fun init(context: Context) {
        MMKV.initialize(context)
    }
}

class KVCacheExt<T>(
    private val key: String,        // 自定义 key，为空则用属性名
    private val value: T,           // 默认值，也用来判断类型
    private val valueRawType: Class<T>  // 类型信息
) : //Any? 表示"任何类都可以使用这个委托"，? 表示还支持可空类型
    //T : 属性类型（被委托的属性是什么类型）	由具体属性决定
    ReadWriteProperty<Any?, T> { //Kotlin 标准库接口，实现它就能用 by 委托

    private val mmkv by lazy { MMKV.defaultMMKV() }
    private var cachedValue: T? = null
    private var hasCache = false

    /*
    thisRef：所属对象
    property：属性信息（能拿到属性名）*/
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!hasCache) {
            cachedValue = findValue(findKey(property))
            hasCache = true
        }
        return cachedValue as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        cachedValue = value
        hasCache = true
        putValue(findKey(property), value)
    }

    //如果没传自定义 key，就用属性名作为 MMKV 的 key
    private fun findKey(property: KProperty<*>) = if (key.isEmpty()) property.name else key

    @Suppress("IMPLICIT_CAST_TO_ANY") //@Suppress 抑制警告
    private fun findValue(key: String): T {
        return when (value) {
            // 从 MMKV 读取数据
            is Long -> mmkv.decodeLong(key)
            is Int -> mmkv.decodeInt(key)
            is Boolean -> mmkv.decodeBool(key)
            is Double -> mmkv.decodeDouble(key)
            is String -> mmkv.decodeString(key)
            is Float -> mmkv.decodeFloat(key)
            //  public <T extends Parcelable> T decodeParcelable(String key, Class<T> clazz)
            is Parcelable -> mmkv.decodeParcelable(key, valueRawType as Class<Parcelable>?)
            else -> throw IllegalArgumentException("Unsupported type.")
        } as T
    }

    private fun putValue(key: String, value: T) {
        when (value) {
            is Long -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value)
            is String -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Parcelable -> mmkv.encode(key, value)
            else -> throw IllegalArgumentException("Unsupported type.")
        }
    }
}

class KVCacheParcelableExt<T : Parcelable?>(
    private val key: String,
    private val valueRawType: Class<T>
) : ReadWriteProperty<Any?, T> { //实现委托接口，让这个类可以被 by 关键字使用

    private val mmkv by lazy { MMKV.defaultMMKV() }
    private var cachedValue: T? = null
    private var hasCache = false

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!hasCache) {
            cachedValue = mmkv.decodeParcelable(findKey(property), valueRawType) ?: null as T
            hasCache = true
        }
        return cachedValue as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        cachedValue = value
        hasCache = true
        mmkv.encode(findKey(property), value)
    }

    private fun findKey(property: KProperty<*>) = key.ifEmpty { property.name }
}