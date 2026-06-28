package me.lenjoy.warsawtransportapp.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import me.lenjoy.warsawtransportapp.api.cache.CacheManager
import me.lenjoy.warsawtransportapp.api.dto.KeyValueDto
import me.lenjoy.warsawtransportapp.api.dto.RoutesResponseDto
import me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto
import me.lenjoy.warsawtransportapp.api.dto.RouteStopDto

/**
 * A Decorator for [WarsawTransportApi] that adds a persistent disk caching layer.
 * All API calls are intercepted: if valid cached data exists (less than 24h old), it is returned.
 * Otherwise, the call is delegated to the [delegate], and the result is cached.
 */
class CachedWarsawTransportApi(
    private val delegate: WarsawTransportApi,
    private val cacheManager: CacheManager
) : WarsawTransportApi {

    override suspend fun getRoutes(): RoutesResponseDto {
        return withCache("routes", RoutesResponseDto.serializer(
            MapSerializer(
                String.serializer(),
                MapSerializer(
                    String.serializer(),
                    MapSerializer(
                        String.serializer(),
                        RouteStopDto.serializer()
                    )
                )
            )
        )) { delegate.getRoutes() }
    }

    override suspend fun getStopLocations(): List<ValuesRowDto> {
        return withCache("stop_locations", ListSerializer(ValuesRowDto.serializer())) {
            delegate.getStopLocations()
        }
    }

    override suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): List<ValuesRowDto> {
        val key = "stop_lines_${stopGroupId}_${stopPoleNumber}"
        return withCache(key, ListSerializer(ValuesRowDto.serializer())) {
            delegate.getStopLines(stopGroupId, stopPoleNumber)
        }
    }

    override suspend fun getDepartures(
        stopGroupId: String,
        stopPoleNumber: String,
        line: String
    ): List<List<KeyValueDto>> {
        val key = "departures_${stopGroupId}_${stopPoleNumber}_${line}"
        val serializer = ListSerializer(ListSerializer(KeyValueDto.serializer()))
        return withCache(key, serializer) {
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
