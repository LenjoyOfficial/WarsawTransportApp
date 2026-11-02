package me.lenjoy.warsawtransportapp.api.cache

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.lenjoy.warsawtransportapp.cache.cacheDir
import me.lenjoy.warsawtransportapp.cache.getCurrentEpochMillis
import okio.FileSystem
import okio.Path

/**
 * Manages persistent disk caching for API responses using Okio.
 * Data is stored in the platform's cache directory with a 24-hour expiration policy.
 * Each entry consists of two files: `{key}.json` for data and `{key}.time` for the fetch timestamp.
 *
 * @param fileSystem The Okio [FileSystem] to use (typically [FileSystem.SYSTEM]).
 * @param manualCacheDir Optional override for the cache directory, useful for unit testing.
 */
class CacheManager(
    private val fileSystem: FileSystem,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val manualCacheDir: Path? = null
) {
    private val expirationDurationMillis: Long = 24 * 60 * 60 * 1000L
    private val cacheBaseDir: Path? = (manualCacheDir ?: cacheDir)?.resolve("api_cache")

    init {
        cacheBaseDir?.let {
            if (!fileSystem.exists(it)) {
                fileSystem.createDirectories(it)
            }
        }
    }

    /**
     * Serializes and saves [data] to disk under the specified [key].
     */
    fun <T> save(key: String, data: T, serializer: KSerializer<T>) {
        val dir = cacheBaseDir ?: return
        val dataFile = dir.resolve("$key.json")
        val timeFile = dir.resolve("$key.time")

        try {
            val jsonString = json.encodeToString(serializer, data)
            fileSystem.write(dataFile) { writeUtf8(jsonString) }
            fileSystem.write(timeFile) { writeUtf8(getCurrentEpochMillis().toString()) }
        } catch (e: Exception) {
            // Log or ignore
        }
    }

    /**
     * Retrieves and deserializes data from disk for the specified [key].
     * Returns null if the data is missing, corrupted, or older than 24 hours.
     */
    fun <T> get(key: String, serializer: KSerializer<T>): T? {
        val dir = cacheBaseDir ?: return null
        val dataFile = dir.resolve("$key.json")
        val timeFile = dir.resolve("$key.time")

        if (!fileSystem.exists(dataFile) || !fileSystem.exists(timeFile)) return null

        try {
            val timestampStr = fileSystem.read(timeFile) { readUtf8() }
            val timestamp = timestampStr.trim().toLongOrNull() ?: return null
            val now = getCurrentEpochMillis()

            if ((now - timestamp) > expirationDurationMillis) {
                clear(key)
                return null
            }

            val jsonString = fileSystem.read(dataFile) { readUtf8() }
            return json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            clear(key)
            return null
        }
    }

    /**
     * Removes the data and timestamp files associated with [key].
     */
    private fun clear(key: String) {
        val dir = cacheBaseDir ?: return
        try {
            fileSystem.delete(dir.resolve("$key.json"))
            fileSystem.delete(dir.resolve("$key.time"))
        } catch (e: Exception) {
            // Ignore
        }
    }
}
