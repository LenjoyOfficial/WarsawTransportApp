package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.RouteResponseDto
import me.lenjoy.warsawtransportapp.api.model.RouteLine
import me.lenjoy.warsawtransportapp.api.model.RouteStop
import me.lenjoy.warsawtransportapp.api.model.TransportType

internal fun parseRouteLines(response: RouteResponseDto): List<RouteLine> {
	val transportType = TransportType.fromApi(response.transportType)
	return response.variants.entries.map { (variantName, variant) ->
		val routeStops = variant.stops.entries.mapNotNull { (sequenceKey, stopDto) ->
			val stopGroup = stopDto.stopGroup ?: return@mapNotNull null
			val stopNumber = stopDto.stopNumber ?: return@mapNotNull null
			RouteStop(
				sequence = sequenceKey.toIntOrNull() ?: 0,
				streetId = stopDto.streetId,
				stopGroupId = stopGroup,
				stopPoleNumber = stopNumber,
				type = stopDto.type,
				distanceMeters = stopDto.distance,
			)
		}
		RouteLine(
			line = response.routeNumber,
			routeName = variantName,
			transportType = transportType,
			stops = routeStops,
		)
	}
}
