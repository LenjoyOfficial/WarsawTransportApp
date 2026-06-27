package me.lenjoy.warsawtransportapp.network

/**
 * Enumeration of API keys required by the application.
 * Matches keys defined in the project's build configuration.
 */
enum class ApiKeyName(val configKey: String) {
	LEGACY("LEGACY_API_KEY"),
	ZTM("ZTM_API_KEY"),
	MAPS("MAPS_API_KEY"),
}

/**
 * Platform-specific utility for retrieving API keys from Build-time configurations.
 */
expect object ApiKeys {
	/**
	 * Returns the value of the requested API key.
	 */
	fun get(name: ApiKeyName): String
}
