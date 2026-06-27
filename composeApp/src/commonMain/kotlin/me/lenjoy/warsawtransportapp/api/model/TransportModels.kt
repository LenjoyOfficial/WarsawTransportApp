package me.lenjoy.warsawtransportapp.api.model

import kotlin.jvm.JvmInline

/**
 * Type-safe identifier for a group of stops (zespół przystankowy).
 */
@JvmInline
value class StopGroupId(val value: String)

/**
 * Type-safe identifier for a specific pole number within a stop group (numer słupka).
 */
@JvmInline
value class StopPoleNumber(val value: String)

/**
 * Type-safe identifier for a public transport line number.
 */
@JvmInline
value class LineNumber(val value: String)

/**
 * Domain model representing a vehicle's real-time position.
 */
data class Vehicle(
	val line: LineNumber,
	val latitude: Double,
	val longitude: Double,
	val vehicleNumber: String,
	val time: String,
	val brigade: String,
)

/**
 * Domain model representing a physical stop location and its metadata.
 *
 * @property validFrom The date from which this stop location is valid (e.g., "2026-01-01 00:00:00").
 */
data class StopLocation(
	val stopGroupId: StopGroupId,
	val stopPoleNumber: StopPoleNumber,
	val stopName: String,
	val streetId: String?,
	val latitude: Double?,
	val longitude: Double?,
	val direction: String?,
	val validFrom: String?,
)

/**
 * Domain model representing a transit line associated with a specific stop.
 */
data class StopLine(
	val line: LineNumber,
)

/**
 * Domain model representing a scheduled departure from a stop.
 *
 * @property serviceTime The parsed time of departure, accounting for day rollovers.
 */
data class Departure(
	val line: LineNumber,
	val route: String?,
	val direction: String?,
	val serviceTime: ServiceTime,
	val brigade: String?,
	val symbol1: String?,
	val symbol2: String?,
)

/**
 * Represents a parsed time of day for transit services, handling values greater than 24 hours.
 *
 * @property raw The original time string from the API (e.g., "26:15:00").
 * @property dayOffset Number of days after the service start (e.g., 1 if time is 26:15:00).
 * @property hourOfDay Hour within the specific day (0-23).
 */
data class ServiceTime(
	val raw: String,
	val dayOffset: Int,
	val hourOfDay: Int,
	val minute: Int,
	val second: Int,
)
