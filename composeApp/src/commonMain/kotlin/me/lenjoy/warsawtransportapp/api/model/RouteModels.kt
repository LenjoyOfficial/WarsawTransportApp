package me.lenjoy.warsawtransportapp.api.model

enum class TransportType {
	Tram, Bus, Train, Unknown;

	companion object {
		fun fromApi(value: String?): TransportType = when (value) {
			"tram" -> Tram
			"bus" -> Bus
			"train" -> Train
			else -> Unknown
		}
	}
}

/**
 * Domain model for a specific stop within a defined [RouteLine].
 *
 * @property sequence The order of the stop in the route.
 * @property distanceMeters Distance in meters from the beginning of the route or previous stop.
 */
data class RouteStop(
	val sequence: Int,
	val streetId: String?,
	val stopGroupId: String,
	val stopPoleNumber: String,
	val type: String?,
	val distanceMeters: Int?,
)

/**
 * Domain model representing a transit line's route.
 *
 * @property line The line number or identifier.
 * @property routeName The specific name of this route variant (e.g., "TD-3BAN").
 * @property transportType The type of transit (tram, bus, train).
 * @property stops The ordered list of stops in this route.
 */
data class RouteLine(
	val line: String,
	val routeName: String,
	val transportType: TransportType,
	val stops: List<RouteStop>,
)
