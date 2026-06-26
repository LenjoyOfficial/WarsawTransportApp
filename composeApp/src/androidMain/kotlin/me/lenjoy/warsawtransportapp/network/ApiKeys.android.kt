package me.lenjoy.warsawtransportapp.network

import me.lenjoy.warsawtransportapp.BuildConfig

actual object ApiKeys {
    actual fun get(name: ApiKeyName): String = when (name) {
        ApiKeyName.LEGACY -> BuildConfig.LEGACY_API_KEY
        ApiKeyName.ZTM -> BuildConfig.ZTM_API_KEY
        ApiKeyName.MAPS -> BuildConfig.MAPS_API_KEY
    }
}

