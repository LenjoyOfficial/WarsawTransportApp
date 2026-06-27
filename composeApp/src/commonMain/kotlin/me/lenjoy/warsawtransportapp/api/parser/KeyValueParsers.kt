package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.KeyValueDto

/**
 * Converts a list of [KeyValueDto] into a [Map] for easier attribute access by key.
 */
internal fun List<KeyValueDto>.asMap(): Map<String, String?> =
	associate { it.key to it.value }

/**
 * Safely retrieves a string value from the map by [key].
 */
internal fun Map<String, String?>.string(key: String): String? = this[key]

/**
 * Safely retrieves and parses a [Double] value from the map by [key].
 */
internal fun Map<String, String?>.double(key: String): Double? = this[key]?.toDoubleOrNull()

/**
 * Safely retrieves and parses an [Int] value from the map by [key].
 */
internal fun Map<String, String?>.int(key: String): Int? = this[key]?.toIntOrNull()
