package me.lenjoy.warsawtransportapp.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResultDto<T>(
	val result: T,
)

@Serializable
data class VehicleDto(
	@SerialName("Lines") val lines: String,
	@SerialName("Lon") val lon: String,
	@SerialName("Lat") val lat: String,
	@SerialName("VehicleNumber") val vehicleNumber: String,
	@SerialName("Time") val time: String,
	@SerialName("Brigade") val brigade: String,
)

@Serializable
data class KeyValueDto(
	val key: String,
	val value: String? = null,
)

@Serializable
data class ValuesRowDto(
	val values: List<KeyValueDto>,
)

@Serializable
data class RouteStopDto(
	@SerialName("ulica_id") val streetId: String? = null,
	@SerialName("nr_zespolu") val stopGroupId: String? = null,
	@SerialName("nr_przystanku") val stopPoleNumber: String? = null,
	val typ: String? = null,
	val odleglosc: Int? = null,
)

typealias RoutesResponseDto = ApiResultDto<Map<String, Map<String, Map<String, RouteStopDto>>>>


