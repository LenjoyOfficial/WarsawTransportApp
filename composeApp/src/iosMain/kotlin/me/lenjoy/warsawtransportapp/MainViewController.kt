package me.lenjoy.warsawtransportapp

import GoogleMaps.GMSServices
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import me.lenjoy.warsawtransportapp.config.BuildKonfig

@ExperimentalForeignApi
fun MainViewController() = ComposeUIViewController {
	// Note: GMSServices initialization should be done here once GoogleMaps SDK is linked
	GMSServices.provideAPIKey(BuildKonfig.MAPS_API_KEY)
	App()
}
