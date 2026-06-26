package me.lenjoy.warsawtransportapp.network

enum class ApiKeyName(val configKey: String) {
    LEGACY("LEGACY_API_KEY"),
    ZTM("ZTM_API_KEY"),
    MAPS("MAPS_API_KEY"),
}

expect object ApiKeys {
    fun get(name: ApiKeyName): String
}

