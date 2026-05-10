package me.lenjoy.warsawtransportapp.network

enum class ApiKeyName(val configKey: String) {
    ZTM("ZTM_API_KEY"),
    MAPS("MAPS_API_KEY"),
    GEOCODING("GEOCODING_API_KEY"),
}

expect object ApiKeys {
    fun get(name: ApiKeyName): String
}

