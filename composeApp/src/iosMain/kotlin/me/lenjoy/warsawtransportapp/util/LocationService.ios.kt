package me.lenjoy.warsawtransportapp.util

import eu.buney.maps.LatLng
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

actual class LocationService {
	@OptIn(ExperimentalForeignApi::class)
	actual suspend fun getCurrentLocation(): LatLng? {
		return suspendCancellableCoroutine { cont ->
			val provider = IosLocationProvider()

			provider.continuation = cont
			provider.manager.requestWhenInUseAuthorization()
			provider.manager.requestLocation()
			cont.invokeOnCancellation {
				provider.continuation = null
			}
		}
	}
}

private class IosLocationProvider : NSObject(), CLLocationManagerDelegateProtocol {
	val manager = CLLocationManager()
	var continuation: Continuation<LatLng?>? = null

	init {
		manager.delegate = this
		manager.desiredAccuracy = kCLLocationAccuracyBest
	}

	override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
		val location = didUpdateLocations.lastOrNull() as? CLLocation
		val result = location?.toLatLng()
		continuation?.resume(result)
		continuation = null
	}

	override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
		continuation?.resume(null)
		continuation = null
	}
}

@OptIn(ExperimentalForeignApi::class)
private fun CLLocation.toLatLng(): LatLng = coordinate.useContents {
	LatLng(
		latitude = latitude,
		longitude = longitude
	)
}
