package me.lenjoy.warsawtransportapp.util

import eu.buney.maps.LatLng
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class LocationService {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): LatLng? = suspendCoroutine { continuation ->
        val locationManager = CLLocationManager()
        locationManager.desiredAccuracy = kCLLocationAccuracyBest

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.lastOrNull() as? platform.CoreLocation.CLLocation
                if (location != null) {
                    val latLng = location.coordinate.useContents {
                        LatLng(latitude, longitude)
                    }
                    locationManager.stopUpdatingLocation()
                    continuation.resume(latLng)
                } else {
                    continuation.resume(null)
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
                locationManager.stopUpdatingLocation()
                continuation.resume(null)
            }
        }

        locationManager.delegate = delegate
        locationManager.startUpdatingLocation()
    }
}
