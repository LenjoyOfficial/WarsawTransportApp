package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.KeyValueDto
import me.lenjoy.warsawtransportapp.api.model.LineNumber
import me.lenjoy.warsawtransportapp.api.model.RouteLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeyValueParsersTest {
	@Test
	fun testAsMapConvertsKeyValueDtoListToMap() {
		val input = listOf(
			KeyValueDto("key1", "value1"),
			KeyValueDto("key2", "value2"),
			KeyValueDto("key3", null),
		)
		val result = input.asMap()
		assertEquals("value1", result["key1"])
		assertEquals("value2", result["key2"])
		assertNull(result["key3"])
	}

	@Test
	fun testStringExtensionReturnsValueForKey() {
		val map = mapOf("name" to "Test Stop", "id" to null)
		assertEquals("Test Stop", map.string("name"))
		assertNull(map.string("id"))
		assertNull(map.string("nonexistent"))
	}

	@Test
	fun testDoubleExtensionConvertsStringToDouble() {
		val map = mapOf("lat" to "52.230155", "lon" to "21.011832", "bad" to "not_a_number")
		assertEquals(52.230155, map.double("lat"))
		assertEquals(21.011832, map.double("lon"))
		assertNull(map.double("bad"))
		assertNull(map.double("nonexistent"))
	}

	@Test
	fun testIntExtensionConvertsStringToInt() {
		val map = mapOf("distance" to "1250", "bad" to "not_a_number")
		assertEquals(1250, map.int("distance"))
		assertNull(map.int("bad"))
		assertNull(map.int("nonexistent"))
	}
}

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
		val values = listOf(
			KeyValueDto("zespol", "7009"),
			KeyValueDto("slupek", "01"),
			KeyValueDto("nazwa_zespolu", "Centrum"),
			KeyValueDto("id_ulicy", "1101"),
			KeyValueDto("szer_geo", "52.230005"),
			KeyValueDto("dlug_geo", "21.011384"),
			KeyValueDto("kierunek", "Muranów"),
			KeyValueDto("obowiazuje_od", "2026-01-01 00:00:00"),
		)
		val result = parseStopLocation(values)
		assertNotNull(result)
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
	fun testParseStopLocationMissingRequiredFields() {
		val values = listOf(
			KeyValueDto("nazwa_zespolu", "Centrum"),
		)
		assertNull(parseStopLocation(values))
	}

	@Test
	fun testParseStopLocationWithoutOptionalFields() {
		val values = listOf(
			KeyValueDto("zespol", "7009"),
			KeyValueDto("slupek", "01"),
			KeyValueDto("nazwa_zespolu", "Centrum"),
		)
		val result = parseStopLocation(values)
		assertNotNull(result)
		assertEquals("Centrum", result.stopName)
		assertNull(result.streetId)
		assertNull(result.latitude)
		assertNull(result.longitude)
	}
}

class DepartureParserTest {
	@Test
	fun testParseDeparturesSuccess() {
		val routes = listOf(
			RouteLine("116", "TP-WIL", listOf()),
		)
		val rows = listOf(
			listOf(
				KeyValueDto("trasa", "TP-WIL"),
				KeyValueDto("kierunek", "Wilanów"),
				KeyValueDto("czas", "14:15:00"),
				KeyValueDto("brygada", "1"),
				KeyValueDto("symbol_1", null),
				KeyValueDto("symbol_2", null),
			),
			listOf(
				KeyValueDto("trasa", "TP-WIL"),
				KeyValueDto("kierunek", "Wilanów"),
				KeyValueDto("czas", "14:30:00"),
				KeyValueDto("brygada", "2"),
			),
		)
		val result = parseDepartures(rows, routes)
		assertEquals(2, result.size)
		assertEquals(LineNumber("116"), result[0].line)
		assertEquals("Wilanów", result[0].direction)
		assertEquals(14, result[0].serviceTime.hourOfDay)
		assertEquals(15, result[0].serviceTime.minute)
		assertEquals("1", result[0].brigade)
	}

	@Test
	fun testParseDeparturesInvalidTime() {
		val routes = listOf(
			RouteLine("116", "TP-WIL", listOf()),
		)
		val rows = listOf(
			listOf(
				KeyValueDto("linia", "116"),
				KeyValueDto("czas", "invalid"),
			),
		)
		val result = parseDepartures(rows, routes)
		assertTrue(result.isEmpty())
	}
}

class StopLinesParserTest {
	@Test
	fun testParseStopLinesSuccess() {
		val rows = listOf(
			me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto(
				values = listOf(KeyValueDto("linia", "23"))
			),
			me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto(
				values = listOf(KeyValueDto("linia", "20"))
			),
			me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto(
				values = listOf(KeyValueDto("linia", "24"))
			),
		)
		val result = parseStopLines(rows)
		assertEquals(3, result.size)
		assertEquals(LineNumber("23"), result[0].line)
		assertEquals(LineNumber("20"), result[1].line)
		assertEquals(LineNumber("24"), result[2].line)
	}

	@Test
	fun testParseStopLinesEmptyList() {
		val result = parseStopLines(emptyList())
		assertEquals(0, result.size)
	}

	@Test
	fun testParseStopLinesMissingLineKey() {
		val rows = listOf(
			me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto(
				values = listOf(KeyValueDto("other_key", "value"))
			),
		)
		val result = parseStopLines(rows)
		assertEquals(0, result.size)
	}
}

