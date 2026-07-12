package me.lenjoy.warsawtransportapp

import android.content.Context
import android.os.Build
import me.lenjoy.warsawtransportapp.util.LocationService

class AndroidPlatform(context: Context) : Platform {
	override val name: String = "Android ${Build.VERSION.SDK_INT}"
	override val isDebug: Boolean = BuildConfig.DEBUG
	override val locationService: LocationService = LocationService(context)
}

private var _platform: Platform? = null

fun initPlatform(context: Context) {
	_platform = AndroidPlatform(context)
}

actual fun getPlatform(): Platform = _platform ?: throw IllegalStateException("Platform not initialized")
