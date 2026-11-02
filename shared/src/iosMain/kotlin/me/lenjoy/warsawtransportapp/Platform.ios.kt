package me.lenjoy.warsawtransportapp

import me.lenjoy.warsawtransportapp.util.LocationService
import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi

class IOSPlatform : Platform {
	override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

	@OptIn(ExperimentalNativeApi::class)
	override val isDebug: Boolean = kotlin.native.Platform.isDebugBinary

	override val locationService: LocationService = LocationService()
}

actual fun getPlatform(): Platform = IOSPlatform()