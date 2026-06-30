package me.lenjoy.warsawtransportapp.util

import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilsTest {
	@Test
	fun testNormalizeForSearch() {
		assertEquals("centrum", "Centrum".normalizeForSearch())
		assertEquals("dworzec", "Dworzec".normalizeForSearch())
		assertEquals("laka", "Łąka".normalizeForSearch())
		assertEquals("zazolc gesla jazn", "Zażółć gęślą jaźń".normalizeForSearch())
		assertEquals("aacceellnnoosszzzz", "ĄąĆćĘęŁłŃńÓóŚśŹźŻż".normalizeForSearch())
	}
}
