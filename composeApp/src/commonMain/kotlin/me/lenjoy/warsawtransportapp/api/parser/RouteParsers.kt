package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.KeyValueDto
import me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto
import me.lenjoy.warsawtransportapp.api.model.Departure
import me.lenjoy.warsawtransportapp.api.model.LineNumber
import me.lenjoy.warsawtransportapp.api.model.ServiceTime
import me.lenjoy.warsawtransportapp.api.model.StopGroupId
import me.lenjoy.warsawtransportapp.api.model.StopLine
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.api.model.StopPoleNumber
import me.lenjoy.warsawtransportapp.api.model.Vehicle

internal fun ValuesRowDto.toMap(): Map<String, String?> = values.asMap()

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

internal fun parseStopLines(rows: List<ValuesRowDto>): List<StopLine> =
    rows.mapNotNull { row ->
        row.toMap().string("linia")?.let { StopLine(LineNumber(it)) }
    }

internal fun parseDepartures(rows: List<List<KeyValueDto>>): List<Departure> =
    rows.mapNotNull { row ->
        val map = row.asMap()
        val line = map.string("linia") ?: return@mapNotNull null
        val serviceTime = map.string("czas")?.let(::parseServiceTime) ?: return@mapNotNull null
        Departure(
            line = LineNumber(line),
            route = map.string("trasa"),
            direction = map.string("kierunek"),
            serviceTime = serviceTime,
            brigade = map.string("brygada"),
            symbol1 = map.string("symbol_1"),
            symbol2 = map.string("symbol_2"),
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


