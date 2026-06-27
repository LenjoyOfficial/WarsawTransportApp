package me.lenjoy.warsawtransportapp.network

import platform.Foundation.NSBundle

actual object ApiKeys {
	private fun read(key: String): String {
		val value = NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String
		return value ?: ""
	}

	actual fun get(name: ApiKeyName): String = when (name) {
		ApiKeyName.LEGACY -> read(name.configKey)
		ApiKeyName.ZTM -> read(name.configKey)
		ApiKeyName.MAPS -> read(name.configKey)
	}
}
