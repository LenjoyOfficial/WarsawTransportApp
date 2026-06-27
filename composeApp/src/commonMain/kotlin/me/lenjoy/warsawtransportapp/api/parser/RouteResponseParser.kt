package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.RoutesResponseDto
import me.lenjoy.warsawtransportapp.api.model.RouteLine
import me.lenjoy.warsawtransportapp.api.model.RouteStop

internal fun parseRouteLines(response: RoutesResponseDto): List<RouteLine> =
	response.result.entries.flatMap { (line, routeNames) ->
		routeNames.entries.map { (routeName, stops) ->
			val routeStops = stops.entries.mapNotNull { (sequenceKey, stopDto) ->
				val stopGroupId = stopDto.stopGroupId ?: return@mapNotNull null
				val stopPoleNumber = stopDto.stopPoleNumber ?: return@mapNotNull null
				RouteStop(
					sequence = sequenceKey.toIntOrNull() ?: 0,
					streetId = stopDto.streetId,
					stopGroupId = stopGroupId,
					stopPoleNumber = stopPoleNumber,
					type = stopDto.typ,
					distanceMeters = stopDto.odleglosc,
				)
			}
			RouteLine(
				line = line,
				routeName = routeName,
				stops = routeStops,
			)
		}
	}
