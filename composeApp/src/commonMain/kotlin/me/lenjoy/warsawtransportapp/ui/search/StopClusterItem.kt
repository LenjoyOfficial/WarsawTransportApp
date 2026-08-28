package me.lenjoy.warsawtransportapp.ui.search

import eu.buney.maps.LatLng
import eu.buney.maps.utils.clustering.ClusterItem
import me.lenjoy.warsawtransportapp.api.model.StopLocation

data class StopClusterItem(
	val stop: StopLocation
) : ClusterItem {
	override val position: LatLng = LatLng(stop.latitude!!, stop.longitude!!)
	override val title: String? get() = stop.stopName
	override val snippet: String? get() = "${stop.stopGroupId.value} ${stop.stopPoleNumber.value}"
	override val zIndex: Float? get() = null
}
