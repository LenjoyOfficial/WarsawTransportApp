package me.lenjoy.warsawtransportapp.api.model

data class RouteStop(
	val sequence: Int,
	val streetId: String?,
	val stopGroupId: String,
	val stopPoleNumber: String,
	val type: String?,
	val distanceMeters: Int?,
)

data class RouteLine(
	val line: String,
	val routeName: String,
	val stops: List<RouteStop>,
)

