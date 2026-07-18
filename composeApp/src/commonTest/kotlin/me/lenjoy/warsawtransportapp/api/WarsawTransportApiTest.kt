package me.lenjoy.warsawtransportapp.api

import kotlinx.coroutines.test.runTest
import me.lenjoy.warsawtransportapp.repository.TransportRepositoryImpl
import kotlin.test.Test

class WarsawTransportApiTest {

	@Test
	fun testGetRoutes() = runTest {
		val repo = TransportRepositoryImpl()
		val result = repo.getRoutes()
		println("GetRoutes Response: ${result[0]}")
	}

	@Test
	fun testGetStopLocations() = runTest {
		val repo = TransportRepositoryImpl()
		val result = repo.getAllStops()
		println("GetStopLocations Response: ${result[0]}")
	}

	@Test
	fun testGetStopLines() = runTest {
		val repo = TransportRepositoryImpl()
		val result = repo.getStopLines("6002", "03")
		println("GetStopLines Response: $result")
	}

	@Test
	fun testGetDepartures() = runTest {
		val repo = TransportRepositoryImpl()

		val routes = repo.getRoutes()
		val result = repo.getDepartures("6002", "03", "15", routes)
		println("GetDepartures Response: $result")
		if (result.isNotEmpty()) {
			println("First Departure: ${result[0]}")
		}
	}
}
