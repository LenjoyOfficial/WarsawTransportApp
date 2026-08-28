package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.RouteResponseDto
import me.lenjoy.warsawtransportapp.api.dto.RouteStopDto
import me.lenjoy.warsawtransportapp.api.dto.RouteVariantDto
import me.lenjoy.warsawtransportapp.api.model.TransportType
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteResponseParserTest {

	@Test
	fun testParseRouteLinesSuccess() {
		val response = RouteResponseDto(
			routeNumber = "1",
			transportType = "tram",
			variants = mapOf(
				"TD-3BAN" to RouteVariantDto(
					name = "TD-3BAN",
					stops = mapOf(
						"1" to RouteStopDto(
							streetId = "2513",
							stopGroup = "R-03",
							stopNumber = "00",
							type = "6",
							distance = 0,
						),
						"2" to RouteStopDto(
							streetId = "1205",
							stopGroup = "3240",
							stopNumber = "04",
							type = "5",
							distance = 245,
						),
					),
				),
			),
		)

		val result = parseRouteLines(response)

		assertEquals(1, result.size)
		val routeLine = result[0]
		assertEquals("1", routeLine.line)
		assertEquals("TD-3BAN", routeLine.routeName)
		assertEquals(TransportType.Tram, routeLine.transportType)
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
	fun testParseRouteLinesBus() {
		val response = RouteResponseDto(
			routeNumber = "523",
			transportType = "bus",
			variants = emptyMap(),
		)

		val result = parseRouteLines(response)

		assertEquals(0, result.size)
	}

	@Test
	fun testParseRouteLinesTransportType() {
		val response = RouteResponseDto(
			routeNumber = "20",
			transportType = "train",
			variants = mapOf(
				"VAR1" to RouteVariantDto(
					name = "VAR1",
					stops = mapOf(
						"1" to RouteStopDto(stopGroup = "G01", stopNumber = "01"),
					),
				),
			),
		)

		val result = parseRouteLines(response)
		assertEquals(TransportType.Train, result[0].transportType)
	}
}
