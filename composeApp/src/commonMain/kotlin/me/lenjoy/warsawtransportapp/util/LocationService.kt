package me.lenjoy.warsawtransportapp.util

import eu.buney.maps.LatLng

expect class LocationService {
    suspend fun getCurrentLocation(): LatLng?
}
