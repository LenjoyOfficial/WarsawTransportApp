package me.lenjoy.warsawtransportapp.api.model

import kotlin.jvm.JvmInline

@JvmInline
value class StopGroupId(val value: String)

@JvmInline
value class StopPoleNumber(val value: String)

@JvmInline
value class LineNumber(val value: String)

data class Vehicle(
	val line: LineNumber,
	val latitude: Double,
	val longitude: Double,
	val vehicleNumber: String,
	val time: String,
	val brigade: String,
)

data class StopLocation(
	val stopGroupId: StopGroupId,
	val stopPoleNumber: StopPoleNumber,
	val stopName: String,
	val streetId: String?,
	val latitude: Double?,
	val longitude: Double?,
	val direction: String?,
	val validFrom: String?,
)

data class StopLine(
	val line: LineNumber,
)

data class Departure(
	val line: LineNumber,
	val route: String?,
	val direction: String?,
	val serviceTime: ServiceTime,
	val brigade: String?,
	val symbol1: String?,
	val symbol2: String?,
)

data class ServiceTime(
	val raw: String,
	val dayOffset: Int,
	val hourOfDay: Int,
	val minute: Int,
	val second: Int,
)


