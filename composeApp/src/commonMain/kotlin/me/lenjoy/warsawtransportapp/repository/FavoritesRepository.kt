package me.lenjoy.warsawtransportapp.repository

import kotlinx.serialization.builtins.ListSerializer
import me.lenjoy.warsawtransportapp.api.cache.CacheManager
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.cache.platformFileSystem

/**
 * Manages the user's favorite transit stops.
 */
interface FavoritesRepository {
	/**
	 * Returns the list of favorite stops.
	 */
	fun getFavorites(): List<StopLocation>

	/**
	 * Toggles the favorite status of a stop.
	 */
	fun toggleFavorite(stop: StopLocation)

	/**
	 * Checks if a stop is currently a favorite.
	 */
	fun isFavorite(stopGroupId: String, stopPoleNumber: String): Boolean
}

/**
 * Implementation of [FavoritesRepository] using [CacheManager] for persistence.
 */
class FavoritesRepositoryImpl(
	private val cacheManager: CacheManager = CacheManager(platformFileSystem)
) : FavoritesRepository {
	private val cacheKey = "favorite_stops"
	private val serializer = ListSerializer(StopLocation.serializer())

	override fun getFavorites(): List<StopLocation> {
		return cacheManager.get(cacheKey, serializer) ?: emptyList()
	}

	override fun toggleFavorite(stop: StopLocation) {
		val current = getFavorites().toMutableList()
		val index = current.indexOfFirst {
			it.stopGroupId == stop.stopGroupId && it.stopPoleNumber == stop.stopPoleNumber
		}

		if (index != -1) {
			current.removeAt(index)
		} else {
			current.add(stop)
		}

		cacheManager.save(cacheKey, current, serializer)
	}

	override fun isFavorite(stopGroupId: String, stopPoleNumber: String): Boolean {
		return getFavorites().any {
			it.stopGroupId.value == stopGroupId && it.stopPoleNumber.value == stopPoleNumber
		}
	}
}
