package me.lenjoy.warsawtransportapp

import me.lenjoy.warsawtransportapp.util.LocationService

interface Platform {
	val name: String
	val isDebug: Boolean
	val locationService: LocationService
}

expect fun getPlatform(): Platform
