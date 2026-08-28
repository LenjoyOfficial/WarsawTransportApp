package me.lenjoy.warsawtransportapp.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import me.lenjoy.warsawtransportapp.api.cache.CacheManager
import me.lenjoy.warsawtransportapp.api.dto.DepartureDto
import me.lenjoy.warsawtransportapp.api.dto.RouteResponseDto
import me.lenjoy.warsawtransportapp.api.dto.StopLinesResponseDto
import me.lenjoy.warsawtransportapp.api.dto.StopLocationDto

class CachedWarsawTransportApi(
	private val delegate: WarsawTransportApi,
	private val cacheManager: CacheManager
) : WarsawTransportApi {

	override suspend fun getRoutes(line: String): RouteResponseDto {
		return withCache("routes_$line", RouteResponseDto.serializer()) {
			delegate.getRoutes(line)
		}
	}

	override suspend fun getStopLocations(): List<StopLocationDto> {
		return withCache("stop_locations", ListSerializer(StopLocationDto.serializer())) {
			delegate.getStopLocations()
		}
	}

	override suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): StopLinesResponseDto {
		val key = "stop_lines_${stopGroupId}_${stopPoleNumber}"
		return withCache(key, StopLinesResponseDto.serializer()) {
			delegate.getStopLines(stopGroupId, stopPoleNumber)
		}
	}

	override suspend fun getDepartures(
		stopGroupId: String,
		stopPoleNumber: String,
		line: String
	): List<DepartureDto> {
		val key = "departures_${stopGroupId}_${stopPoleNumber}_${line}"
		return withCache(key, ListSerializer(DepartureDto.serializer())) {
			delegate.getDepartures(stopGroupId, stopPoleNumber, line)
		}
	}

	private suspend fun <T> withCache(
		key: String,
		serializer: KSerializer<T>,
		fetch: suspend () -> T
	): T {
		val cached = cacheManager.get(key, serializer)
		if (cached != null) {
			println("Cache HIT for $key")
			return cached
		}
		println("Cache MISS for $key")
		val result = fetch()
		cacheManager.save(key, result, serializer)
		return result
	}
}
