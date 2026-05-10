package me.lenjoy.warsawtransportapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform