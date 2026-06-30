package me.lenjoy.warsawtransportapp.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level wrapper for API responses from the Warsaw Open Data API.
 */
@Serializable
data class ApiResultDto<T>(
	val result: T,
)

/**
 * A simple key-value pair used in many Warsaw API responses where data is returned as a list of attributes.
 */
@Serializable
data class KeyValueDto(
	val key: String,
	val value: String? = null,
)

/**
 * Represents a single row of data from the API, composed of multiple [KeyValueDto] objects.
 */
@Serializable
data class ValuesRowDto(
	val values: List<KeyValueDto>,
)

/**
 * Data Transfer Object representing a vehicle's real-time position and state.
 *
 * @property lines The transit line number.
 * @property lon Longitude as a string.
 * @property lat Latitude as a string.
 * @property vehicleNumber Unique identifier for the vehicle.
 * @property time The timestamp of the measurement (e.g., "2026-06-24 14:30:00").
 * @property brigade The brigade/shift identifier.
 */
@Serializable
data class VehicleDto(
	@SerialName("Lines") val lines: String,
	@SerialName("Lon") val lon: String,
	@SerialName("Lat") val lat: String,
	@SerialName("VehicleNumber") val vehicleNumber: String,
	@SerialName("Time") val time: String,
	@SerialName("Brigade") val brigade: String,
)

/**
 * Data Transfer Object for a stop within a route definition.
 *
 * @property streetId Identifier for the street (ulica_id).
 * @property stopGroupId Identifier for the stop group (nr_zespolu).
 * @property stopPoleNumber The specific pole number within the group (nr_przystanku).
 * @property typ The stop type.
 * @property odleglosc Distance in meters from the previous stop.
 */
@Serializable
data class RouteStopDto(
	@SerialName("ulica_id") val streetId: String? = null,
	@SerialName("nr_zespolu") val stopGroupId: String? = null,
	@SerialName("nr_przystanku") val stopPoleNumber: String? = null,
	val typ: String? = null,
	val odleglosc: Int? = null,
)

/**
 * Type alias for the complex nested map structure returned by the public transport routes API.
 * Maps: route-id -> route-name -> sequence -> stop details.
 */
typealias RoutesResponseDto = ApiResultDto<Map<String, Map<String, Map<String, RouteStopDto>>>>
