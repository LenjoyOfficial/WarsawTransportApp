package me.lenjoy.warsawtransportapp.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
data class RouteStopDto(
	@SerialName("street_id") val streetId: String? = null,
	@SerialName("stop_group") val stopGroup: String? = null,
	@SerialName("stop_number") val stopNumber: String? = null,
	@SerialName("type") val type: String? = null,
	@SerialName("distance") val distance: Int? = null,
)

@Serializable
data class RouteVariantDto(
	val name: String,
	val stops: Map<String, RouteStopDto>,
)

@Serializable
data class RouteResponseDto(
	@SerialName("route_number") val routeNumber: String,
	@SerialName("transport_type") val transportType: String,
	val variants: Map<String, RouteVariantDto>,
)

@Serializable
data class StopLocationDto(
	@SerialName("stop_group") val stopGroup: String,
	@SerialName("stop_pole") val stopPole: String,
	@SerialName("stop_group_name") val stopGroupName: String,
	@SerialName("transport_type") val transportType: String? = null,
	@SerialName("street_id") val streetId: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
	val direction: String? = null,
	@SerialName("valid_from") val validFrom: String? = null,
)

@Serializable
data class StopLinesResponseDto(
	@SerialName("stop_id") val stopId: String,
	@SerialName("stop_number") val stopNumber: String,
	val lines: List<String>,
)

@Serializable
data class DepartureDto(
	@SerialName("departure_time") val departureTime: String,
	val brigade: String? = null,
	val direction: String? = null,
	val route: String? = null,
	@SerialName("symbol_1") val symbol1: String? = null,
	@SerialName("symbol_2") val symbol2: String? = null,
)
