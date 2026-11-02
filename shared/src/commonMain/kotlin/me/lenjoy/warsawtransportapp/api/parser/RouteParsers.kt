package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.DepartureDto
import me.lenjoy.warsawtransportapp.api.dto.StopLinesResponseDto
import me.lenjoy.warsawtransportapp.api.dto.StopLocationDto
import me.lenjoy.warsawtransportapp.api.model.Departure
import me.lenjoy.warsawtransportapp.api.model.LineNumber
import me.lenjoy.warsawtransportapp.api.model.ServiceTime
import me.lenjoy.warsawtransportapp.api.model.StopGroupId
import me.lenjoy.warsawtransportapp.api.model.StopLine
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.api.model.StopPoleNumber
import me.lenjoy.warsawtransportapp.api.model.TransportType
import me.lenjoy.warsawtransportapp.api.model.Vehicle

internal fun parseServiceTime(raw: String): ServiceTime? {
	val parts = raw.split(":")
	if (parts.size != 3) return null
	val hour = parts[0].toIntOrNull() ?: return null
	val minute = parts[1].toIntOrNull() ?: return null
	val second = parts[2].toIntOrNull() ?: return null
	return ServiceTime(
		raw = raw,
		dayOffset = hour / 24,
		hourOfDay = hour % 24,
		minute = minute,
		second = second,
	)
}

internal fun parseStopLocation(dto: StopLocationDto): StopLocation {
	return StopLocation(
		stopGroupId = StopGroupId(dto.stopGroup),
		stopPoleNumber = StopPoleNumber(dto.stopPole),
		stopName = dto.stopGroupName,
		type = TransportType.fromApi(dto.transportType),
		streetId = dto.streetId,
		latitude = dto.latitude,
		longitude = dto.longitude,
		direction = dto.direction,
		validFrom = dto.validFrom,
	)
}

internal fun parseStopLines(response: StopLinesResponseDto): List<StopLine> =
	response.lines.map { StopLine(LineNumber(it)) }

internal fun parseDepartures(rows: List<DepartureDto>, line: String): List<Departure> =
	rows.mapNotNull { row ->
		val serviceTime = parseServiceTime(row.departureTime) ?: return@mapNotNull null
		Departure(
			line = LineNumber(line),
			route = row.route,
			direction = row.direction,
			serviceTime = serviceTime,
			brigade = row.brigade,
			symbol1 = row.symbol1,
			symbol2 = row.symbol2,
		)
	}

internal fun parseVehicleDto(
	line: String,
	lat: String,
	lon: String,
	vehicleNumber: String,
	time: String,
	brigade: String,
): Vehicle? {
	return Vehicle(
		line = LineNumber(line),
		latitude = lat.toDoubleOrNull() ?: return null,
		longitude = lon.toDoubleOrNull() ?: return null,
		vehicleNumber = vehicleNumber,
		time = time,
		brigade = brigade,
	)
}
