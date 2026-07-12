package me.lenjoy.warsawtransportapp.util

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import eu.buney.maps.LatLng
import kotlinx.coroutines.tasks.await

actual class LocationService(private val context: Context) {
	@SuppressLint("MissingPermission")
	actual suspend fun getCurrentLocation(): LatLng? {
		val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

		return try {
			val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()

			location?.let { LatLng(it.latitude, it.longitude) }

		} catch (e: Exception) {
			null
		}
	}
}
