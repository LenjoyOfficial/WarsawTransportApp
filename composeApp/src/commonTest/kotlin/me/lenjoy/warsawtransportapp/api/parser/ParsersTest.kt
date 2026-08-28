package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.DepartureDto
import me.lenjoy.warsawtransportapp.api.dto.StopLinesResponseDto
import me.lenjoy.warsawtransportapp.api.dto.StopLocationDto
import me.lenjoy.warsawtransportapp.api.model.LineNumber
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ServiceTimeParserTest {
	@Test
	fun testParseServiceTimeRegularHour() {
		val result = parseServiceTime("14:30:45")
		assertNotNull(result)
		assertEquals("14:30:45", result.raw)
		assertEquals(0, result.dayOffset)
		assertEquals(14, result.hourOfDay)
		assertEquals(30, result.minute)
		assertEquals(45, result.second)
	}

	@Test
	fun testParseServiceTimeNightLine() {
		val result = parseServiceTime("26:15:00")
		assertNotNull(result)
		assertEquals("26:15:00", result.raw)
		assertEquals(1, result.dayOffset)
		assertEquals(2, result.hourOfDay)
		assertEquals(15, result.minute)
		assertEquals(0, result.second)
	}

	@Test
	fun testParseServiceTimeInvalidFormat() {
		assertNull(parseServiceTime("invalid"))
		assertNull(parseServiceTime("14:30"))
		assertNull(parseServiceTime(""))
		assertNull(parseServiceTime("14:30:00:00"))
	}

	@Test
	fun testParseServiceTimeMidnight() {
		val result = parseServiceTime("00:00:00")
		assertNotNull(result)
		assertEquals(0, result.dayOffset)
		assertEquals(0, result.hourOfDay)
	}

	@Test
	fun testParseServiceTime23Hour() {
		val result = parseServiceTime("23:59:59")
		assertNotNull(result)
		assertEquals(0, result.dayOffset)
		assertEquals(23, result.hourOfDay)
	}
}

class StopLocationParserTest {
	@Test
	fun testParseStopLocationSuccess() {
		val dto = StopLocationDto(
			stopGroup = "7009",
			stopPole = "01",
			stopGroupName = "Centrum",
			streetId = "1101",
			latitude = 52.230005,
			longitude = 21.011384,
			direction = "Muranów",
			validFrom = "2026-01-01 00:00:00",
		)
		val result = parseStopLocation(dto)
		assertEquals("7009", result.stopGroupId.value)
		assertEquals("01", result.stopPoleNumber.value)
		assertEquals("Centrum", result.stopName)
		assertEquals("1101", result.streetId)
		assertEquals(52.230005, result.latitude)
		assertEquals(21.011384, result.longitude)
		assertEquals("Muranów", result.direction)
		assertEquals("2026-01-01 00:00:00", result.validFrom)
	}

	@Test
	fun testParseStopLocationWithoutOptionalFields() {
		val dto = StopLocationDto(
			stopGroup = "7009",
			stopPole = "01",
			stopGroupName = "Centrum",
		)
		val result = parseStopLocation(dto)
		assertEquals("Centrum", result.stopName)
		assertNull(result.streetId)
		assertNull(result.latitude)
		assertNull(result.longitude)
	}
}

class DepartureParserTest {
	@Test
	fun testParseDeparturesSuccess() {
		val rows = listOf(
			DepartureDto(
				departureTime = "14:15:00",
				brigade = "1",
				direction = "Wilanów",
				route = "TP-WIL",
			),
			DepartureDto(
				departureTime = "14:30:00",
				brigade = "2",
				direction = "Wilanów",
				route = "TP-WIL",
			),
		)
		val result = parseDepartures(rows, "116")
		assertEquals(2, result.size)
		assertEquals(LineNumber("116"), result[0].line)
		assertEquals("Wilanów", result[0].direction)
		assertEquals(14, result[0].serviceTime.hourOfDay)
		assertEquals(15, result[0].serviceTime.minute)
		assertEquals("1", result[0].brigade)
	}

	@Test
	fun testParseDeparturesInvalidTime() {
		val rows = listOf(
			DepartureDto(departureTime = "invalid"),
		)
		val result = parseDepartures(rows, "116")
		assertTrue(result.isEmpty())
	}
}

class StopLinesParserTest {
	@Test
	fun testParseStopLinesSuccess() {
		val response = StopLinesResponseDto(
			stopId = "5070",
			stopNumber = "03",
			lines = listOf("23", "20", "24"),
		)
		val result = parseStopLines(response)
		assertEquals(3, result.size)
		assertEquals(LineNumber("23"), result[0].line)
		assertEquals(LineNumber("20"), result[1].line)
		assertEquals(LineNumber("24"), result[2].line)
	}

	@Test
	fun testParseStopLinesEmptyList() {
		val response = StopLinesResponseDto(
			stopId = "5070",
			stopNumber = "03",
			lines = emptyList(),
		)
		val result = parseStopLines(response)
		assertEquals(0, result.size)
	}
}
