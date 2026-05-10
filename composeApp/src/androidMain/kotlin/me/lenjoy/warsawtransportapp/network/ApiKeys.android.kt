package me.lenjoy.warsawtransportapp.network

import me.lenjoy.warsawtransportapp.BuildConfig

actual object ApiKeys {
    actual fun get(name: ApiKeyName): String = when (name) {
        ApiKeyName.ZTM -> BuildConfig.ZTM_API_KEY
        ApiKeyName.MAPS -> BuildConfig.MAPS_API_KEY
        ApiKeyName.GEOCODING -> BuildConfig.GEOCODING_API_KEY
    }
}

