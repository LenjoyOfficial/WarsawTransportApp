package me.lenjoy.warsawtransportapp.api

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import me.lenjoy.warsawtransportapp.api.cache.CacheManager
import me.lenjoy.warsawtransportapp.api.dto.KeyValueDto
import me.lenjoy.warsawtransportapp.api.dto.RoutesResponseDto
import me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto
import me.lenjoy.warsawtransportapp.api.dto.RouteStopDto
import okio.FileSystem

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
        val key = "routes"
        val serializer = RoutesResponseDto.serializer(
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
        )
        val cached = cacheManager.get(key, serializer)
        if (cached != null) {
            println("Cache HIT for $key")
            return cached
        }
        println("Cache MISS for $key")
        val result = delegate.getRoutes()
        cacheManager.save(key, result, serializer)
        return result
    }

    override suspend fun getStopLocations(): List<ValuesRowDto> {
        val key = "stop_locations"
        val serializer = ListSerializer(ValuesRowDto.serializer())
        val cached = cacheManager.get(key, serializer)
        if (cached != null) {
            println("Cache HIT for $key")
            return cached
        }
        println("Cache MISS for $key")
        val result = delegate.getStopLocations()
        cacheManager.save(key, result, serializer)
        return result
    }

    override suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): List<ValuesRowDto> {
        val key = "stop_lines_${stopGroupId}_${stopPoleNumber}"
        val serializer = ListSerializer(ValuesRowDto.serializer())
        val cached = cacheManager.get(key, serializer)
        if (cached != null) {
            println("Cache HIT for $key")
            return cached
        }
        println("Cache MISS for $key")
        val result = delegate.getStopLines(stopGroupId, stopPoleNumber)
        cacheManager.save(key, result, serializer)
        return result
    }

    override suspend fun getDepartures(
        stopGroupId: String,
        stopPoleNumber: String,
        line: String
    ): List<List<KeyValueDto>> {
        val key = "departures_${stopGroupId}_${stopPoleNumber}_${line}"
        val serializer = ListSerializer(ListSerializer(KeyValueDto.serializer()))
        val cached = cacheManager.get(key, serializer)
        if (cached != null) {
            println("Cache HIT for $key")
            return cached
        }
        println("Cache MISS for $key")
        val result = delegate.getDepartures(stopGroupId, stopPoleNumber, line)
        cacheManager.save(key, result, serializer)
        return result
    }
}
