package me.lenjoy.warsawtransportapp.network

import platform.Foundation.NSBundle

actual object ApiKeys {
    private fun read(key: String): String {
        val value = NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String
        return value ?: ""
    }

    actual fun get(name: ApiKeyName): String = read(name.configKey)
}

