package me.lenjoy.warsawtransportapp.api.cache

import kotlinx.serialization.Serializable
import me.lenjoy.warsawtransportapp.cache.platformFileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
data class TestData(val value: String)

class CacheManagerTest {
	private val fileSystem = platformFileSystem
	private val testCacheDir = "/tmp/test_cache".toPath()
	private val cacheManager = CacheManager(fileSystem, manualCacheDir = testCacheDir)

	@Test
	fun testSaveAndGet() {
		val data = TestData("test_value")
		cacheManager.save("test_key", data, TestData.serializer())

		val retrieved = cacheManager.get("test_key", TestData.serializer())
		assertEquals(data, retrieved)
	}

	@Test
	fun testCacheMiss() {
		val retrieved = cacheManager.get("non_existent", TestData.serializer())
		assertNull(retrieved)
	}
}
