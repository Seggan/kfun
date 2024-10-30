package io.github.seggan.sf4k.serial.blockstorage

import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Location
import org.bukkit.block.Block
import kotlin.math.abs

/**
 * Gets a value from the [Location] with the given key.
 *
 * @receiver The [Location] to get the value from.
 * @param key The key to get the value from.
 * @param strategy The deserialization strategy to use. Defaults to [serializer].
 * @param settings The settings to use when deserializing the value.
 * @param T The type of the value.
 * @return The value, or null if it does not exist.
 */
inline fun <reified T> Location.getBlockStorage(
    key: String,
    strategy: DeserializationStrategy<T> = BukkitSerializerRegistry.serializer<T>(),
    settings: BlockStorageSettings = BlockStorageSettings()
): T? {
    val encoded = BlockStorage.getLocationInfo(this, key) ?: return null
    return BlockStorageDecoder.decode(strategy, encoded, settings)
}

/**
 * Gets a value from the [Block] with the given key.
 *
 * @receiver The [Block] to get the value from.
 * @param key The key to get the value from.
 * @param strategy The deserialization strategy to use. Defaults to [serializer].
 * @param settings The settings to use when deserializing the value.
 * @param T The type of the value.
 * @return The value, or null if it does not exist.
 */
inline fun <reified T> Block.getBlockStorage(
    key: String,
    strategy: DeserializationStrategy<T> = BukkitSerializerRegistry.serializer<T>(),
    settings: BlockStorageSettings = BlockStorageSettings()
): T? = location.getBlockStorage(key, strategy, settings)

/**
 * Sets a value in the [Location] with the given key.
 *
 * @receiver The [Location] to set the value in.
 * @param key The key to set the value at.
 * @param value The value to set.
 * @param strategy The serialization strategy to use. Defaults to [serializer].
 * @param settings The settings to use when serializing the value.
 * @param T The type of the value.
 */
inline fun <reified T> Location.setBlockStorage(
    key: String,
    value: T,
    strategy: SerializationStrategy<T> = BukkitSerializerRegistry.serializer<T>(),
    settings: BlockStorageSettings = BlockStorageSettings()
) {
    val encoded = BlockStorageEncoder.encode(strategy, value, settings)
    BlockStorage.addBlockInfo(this, key, encoded)
}

/**
 * Sets a value in the [Block] with the given key.
 *
 * @receiver The [Block] to set the value in.
 * @param key The key to set the value at.
 * @param value The value to set.
 * @param strategy The serialization strategy to use. Defaults to [serializer].
 * @param settings The settings to use when serializing the value.
 * @param T The type of the value.
 */
inline fun <reified T> Block.setBlockStorage(
    key: String,
    value: T,
    strategy: SerializationStrategy<T> = BukkitSerializerRegistry.serializer<T>(),
    settings: BlockStorageSettings = BlockStorageSettings()
) = location.setBlockStorage(key, value, strategy, settings)

private const val BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

internal fun Long.toBase62(): String {
    val sb = StringBuilder()
    var num = abs(this)
    while (num > 0) {
        sb.append(BASE62[(num % 62).toInt()])
        num /= 62
    }
    if (this < 0) {
        sb.append('-')
    }
    return sb.reverse().toString()
}

internal fun CharSequence.fromBase62(): Long {
    if (isEmpty()) return 0
    val negative = this[0] == '-'
    val num = if (negative) substring(1) else this
    val result = num.fold(0L) { acc, c -> acc * 62 + BASE62.indexOf(c) }
    return if (negative) -result else result
}

internal fun StringBuilder.nom(): Char {
    val c = this[0]
    deleteCharAt(0)
    return c
}

internal fun StringBuilder.nom(n: Int): String {
    val s = StringBuilder()
    repeat(n) {
        s.append(nom())
    }
    return s.toString()
}

internal inline fun StringBuilder.nomWhile(predicate: (Char) -> Boolean): String {
    val sb = StringBuilder()
    while (true) {
        val c = nom()
        if (predicate(c)) {
            sb.append(c)
        } else {
            break
        }
    }
    return sb.toString()
}