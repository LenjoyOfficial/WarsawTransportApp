package me.lenjoy.warsawtransportapp.api.parser

import me.lenjoy.warsawtransportapp.api.dto.KeyValueDto

internal fun List<KeyValueDto>.asMap(): Map<String, String?> =
	associate { it.key to it.value }

internal fun Map<String, String?>.string(key: String): String? = this[key]

internal fun Map<String, String?>.double(key: String): Double? = this[key]?.toDoubleOrNull()

internal fun Map<String, String?>.int(key: String): Int? = this[key]?.toIntOrNull()

