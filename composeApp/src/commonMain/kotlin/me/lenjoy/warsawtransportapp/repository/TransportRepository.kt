package me.lenjoy.warsawtransportapp.repository

import me.lenjoy.warsawtransportapp.api.CachedWarsawTransportApi
import me.lenjoy.warsawtransportapp.api.WarsawTransportApi
import me.lenjoy.warsawtransportapp.api.WarsawTransportApiImpl
import me.lenjoy.warsawtransportapp.api.cache.CacheManager
import me.lenjoy.warsawtransportapp.api.model.Departure
import me.lenjoy.warsawtransportapp.api.model.RouteLine
import me.lenjoy.warsawtransportapp.api.model.StopLine
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.api.parser.parseDepartures
import me.lenjoy.warsawtransportapp.api.parser.parseRouteLines
import me.lenjoy.warsawtransportapp.api.parser.parseStopLines
import me.lenjoy.warsawtransportapp.api.parser.parseStopLocation
import me.lenjoy.warsawtransportapp.cache.platformFileSystem

/**
 * Main entry point for fetching transit data in the application.
 * Returns domain models and hides implementation details like caching or raw API structure.
 */
interface TransportRepository {
	/**
	 * Returns all available routes.
	 */
	suspend fun getRoutes(): List<RouteLine>

	/**
	 * Returns all transit stop locations.
	 */
	suspend fun getAllStops(): List<StopLocation>

	/**
	 * Returns the list of lines serving a specific stop.
	 */
	suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): List<StopLine>

	/**
	 * Returns scheduled departures for a specific line at a stop.
	 */
	suspend fun getDepartures(stopGroupId: String, stopPoleNumber: String, line: String, routes: List<RouteLine>): List<Departure>
}

/**
 * Implementation of [TransportRepository] that uses a cached API by default.
 */
class TransportRepositoryImpl(
	private val api: WarsawTransportApi = CachedWarsawTransportApi(
		delegate = WarsawTransportApiImpl(),
		cacheManager = CacheManager(platformFileSystem)
	),
) : TransportRepository {

	override suspend fun getRoutes(): List<RouteLine> {
		val raw = api.getRoutes()
		return parseRouteLines(raw)
	}

	override suspend fun getAllStops(): List<StopLocation> {
		val raw = api.getStopLocations()
		return raw.mapNotNull { row -> parseStopLocation(row.values) }
	}

	override suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): List<StopLine> {
		val raw = api.getStopLines(stopGroupId, stopPoleNumber)
		return parseStopLines(raw)
	}

	override suspend fun getDepartures(
		stopGroupId: String,
		stopPoleNumber: String,
		line: String,
		routes: List<RouteLine>
	): List<Departure> {
		val raw = api.getDepartures(stopGroupId, stopPoleNumber, line)
		return parseDepartures(raw, routes, line)
	}
}
