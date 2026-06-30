package me.lenjoy.warsawtransportapp.ui.search

import me.lenjoy.warsawtransportapp.api.model.StopGroupId
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.api.model.StopPoleNumber
import me.lenjoy.warsawtransportapp.util.normalizeForSearch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchLogicTest {
	private val stops = listOf(
		createStop("Centrum", "01"),
		createStop("Centrum", "02"),
		createStop("Dworzec Centralny", "01"),
		createStop("Łąka", "01")
	)

	@Test
	fun testFiltering() {
		val query = "centrum".normalizeForSearch()
		val result = stops.filter { it.stopName.normalizeForSearch().contains(query) }
		assertEquals(2, result.size) // 2x Centrum
	}

	@Test
	fun testAccentInsensitiveFiltering() {
		val query = "laka".normalizeForSearch()
		val result = stops.filter { it.stopName.normalizeForSearch().contains(query) }
		assertEquals(1, result.size)
		assertEquals("Łąka", result[0].stopName)
	}

	@Test
	fun testEmptyQuery() {
		val query = "".normalizeForSearch()
		assertTrue(query.isEmpty())
	}

	private fun createStop(name: String, pole: String) = StopLocation(
		stopGroupId = StopGroupId("1001"),
		stopPoleNumber = StopPoleNumber(pole),
		stopName = name,
		streetId = null,
		latitude = 0.0,
		longitude = 0.0,
		direction = null,
		validFrom = null
	)
}
