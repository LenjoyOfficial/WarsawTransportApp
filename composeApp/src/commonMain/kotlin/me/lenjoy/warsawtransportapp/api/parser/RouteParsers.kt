package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.KeyValueDto
import me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto
import me.lenjoy.warsawtransportapp.api.model.Departure
import me.lenjoy.warsawtransportapp.api.model.LineNumber
import me.lenjoy.warsawtransportapp.api.model.RouteLine
import me.lenjoy.warsawtransportapp.api.model.ServiceTime
import me.lenjoy.warsawtransportapp.api.model.StopGroupId
import me.lenjoy.warsawtransportapp.api.model.StopLine
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.api.model.StopPoleNumber
import me.lenjoy.warsawtransportapp.api.model.Vehicle

/**
 * Utility to convert [ValuesRowDto] into a [Map] of attributes.
 */
internal fun ValuesRowDto.toMap(): Map<String, String?> = values.asMap()

/**
 * Parses a raw time string from the API (e.g., "26:15:00") into a [ServiceTime] domain model.
 * Handles hours >= 24 by incrementing the day offset.
 */
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

/**
 * Maps raw key-value data from the API into a [StopLocation] domain model.
 */
internal fun parseStopLocation(values: List<KeyValueDto>): StopLocation? {
	val map = values.asMap()
	val stopGroupId = map.string("zespol") ?: return null
	val stopPoleNumber = map.string("slupek") ?: return null
	val stopName = map.string("nazwa_zespolu") ?: return null
	return StopLocation(
		stopGroupId = StopGroupId(stopGroupId),
		stopPoleNumber = StopPoleNumber(stopPoleNumber),
		stopName = stopName,
		streetId = map.string("id_ulicy"),
		latitude = map.double("szer_geo"),
		longitude = map.double("dlug_geo"),
		direction = map.string("kierunek"),
		validFrom = map.string("obowiazuje_od"),
	)
}

/**
 * Parses a list of [ValuesRowDto] into a list of [StopLine] domain models.
 */
internal fun parseStopLines(rows: List<ValuesRowDto>): List<StopLine> =
	rows.mapNotNull { row ->
		row.toMap().string("linia")?.let { StopLine(LineNumber(it)) }
	}

/**
 * Parses raw departure data from the API into a list of [Departure] domain models.
 * Requires [routes] to resolve route info and [line] to set the line number.
 */
internal fun parseDepartures(rows: List<List<KeyValueDto>>, routes: List<RouteLine>, line: String): List<Departure> {
	val list = rows.mapNotNull { row ->
		val map = row.asMap()
		val route = map.string("trasa")
		val serviceTime = map.string("czas")?.let(::parseServiceTime) ?: return@mapNotNull null
		Departure(
			line = LineNumber(line),
			route = route,
			direction = map.string("kierunek"),
			serviceTime = serviceTime,
			brigade = map.string("brygada"),
			symbol1 = map.string("symbol_1"),
			symbol2 = map.string("symbol_2"),
		)
	}

	return list
}

/**
 * Maps raw vehicle data fields into a [Vehicle] domain model.
 */
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
