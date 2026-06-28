package me.lenjoy.warsawtransportapp

import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi

class IOSPlatform : Platform {
	override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

	@OptIn(ExperimentalNativeApi::class)
	override val isDebug: Boolean = kotlin.native.Platform.isDebugBinary
}

actual fun getPlatform(): Platform = IOSPlatform()