package me.lenjoy.warsawtransportapp

interface Platform {
	val name: String
	val isDebug: Boolean
}

expect fun getPlatform(): Platform
