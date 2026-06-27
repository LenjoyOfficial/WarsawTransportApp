package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.ApiResultDto
import me.lenjoy.warsawtransportapp.api.dto.RouteStopDto
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteResponseParserTest {

	@Test
	fun testParseRouteLinesSuccess() {
		val response = ApiResultDto(
			mapOf(
				"1" to mapOf(
					"TD-3BAN" to mapOf(
						"1" to RouteStopDto(
							streetId = "2513",
							stopGroupId = "R-03",
							stopPoleNumber = "00",
							typ = "6",
							odleglosc = 0
						),
						"2" to RouteStopDto(
							streetId = "1205",
							stopGroupId = "3240",
							stopPoleNumber = "04",
							typ = "5",
							odleglosc = 245
						)
					)
				)
			)
		)

		val result = parseRouteLines(response)

		assertEquals(1, result.size)
		val routeLine = result[0]
		assertEquals("1", routeLine.line)
		assertEquals("TD-3BAN", routeLine.routeName)
		assertEquals(2, routeLine.stops.size)

		val stop1 = routeLine.stops.find { it.sequence == 1 }
		assertEquals("2513", stop1?.streetId)
		assertEquals("R-03", stop1?.stopGroupId)
		assertEquals("00", stop1?.stopPoleNumber)
		assertEquals("6", stop1?.type)
		assertEquals(0, stop1?.distanceMeters)

		val stop2 = routeLine.stops.find { it.sequence == 2 }
		assertEquals("1205", stop2?.streetId)
		assertEquals("3240", stop2?.stopGroupId)
		assertEquals("04", stop2?.stopPoleNumber)
		assertEquals("5", stop2?.type)
		assertEquals(245, stop2?.distanceMeters)
	}

	@Test
	fun testParseRouteLinesEmpty() {
		val result = parseRouteLines(ApiResultDto(emptyMap()))
		assertEquals(0, result.size)
	}
}
