package me.lenjoy.warsawtransportapp.util

private val POLISH_CHAR_MAPPING = mapOf(
	'ą' to 'a', 'ć' to 'c', 'ę' to 'e', 'ł' to 'l', 'ń' to 'n', 'ó' to 'o', 'ś' to 's', 'ź' to 'z', 'ż' to 'z',
	'Ą' to 'a', 'Ć' to 'c', 'Ę' to 'e', 'Ł' to 'l', 'Ń' to 'n', 'Ó' to 'o', 'Ś' to 's', 'Ź' to 'z', 'Ż' to 'z'
)

/**
 * Normalizes a string for search by converting to lowercase and replacing Polish accented characters
 * with their non-accented counterparts.
 */
fun String.normalizeForSearch(): String {
	return this.map { POLISH_CHAR_MAPPING[it] ?: it.lowercaseChar() }.joinToString("")
}
