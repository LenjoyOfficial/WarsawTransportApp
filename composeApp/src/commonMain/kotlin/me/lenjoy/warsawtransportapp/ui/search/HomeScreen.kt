package me.lenjoy.warsawtransportapp.ui.search

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import eu.buney.maps.CameraPosition
import eu.buney.maps.CameraUpdateFactory
import eu.buney.maps.GoogleMap
import eu.buney.maps.LatLng
import eu.buney.maps.MapProperties
import eu.buney.maps.MapStyleOptions
import eu.buney.maps.rememberCameraPositionState
import kotlinx.coroutines.launch
import me.lenjoy.warsawtransportapp.ScreenEntry
import me.lenjoy.warsawtransportapp.SettingsScreenEntry
import me.lenjoy.warsawtransportapp.getPlatform
import org.jetbrains.compose.resources.stringResource
import warsawtransportapp.composeapp.generated.resources.Res
import warsawtransportapp.composeapp.generated.resources.location_permission_denied

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
	searchBar: @Composable () -> Unit,
	onOpenScreen: (ScreenEntry) -> Unit
) {
	BottomSheetScaffold(
		sheetContent = {
			Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
				Text("hi i am a sheet")

				Card(
					modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(0.33f).padding(16.dp),
					onClick = {
						onOpenScreen(SettingsScreenEntry)
					}
				) {
					Column(
						modifier = Modifier.fillMaxWidth().padding(8.dp),
						verticalArrangement = Arrangement.Center,
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Icon(imageVector = SettingsScreenEntry.icon, contentDescription = null)
						Text(stringResource(SettingsScreenEntry.name))
					}
				}
			}
		}
	) {
		val warsaw = LatLng(52.237049, 21.017532)
		val cameraPositionState = rememberCameraPositionState {
			position = CameraPosition(target = warsaw, zoom = 12f)
		}

		val factory = rememberPermissionsControllerFactory()
		val controller = remember(factory) { factory.createPermissionsController() }
		val coroutineScope = rememberCoroutineScope()
		val snackbarHostState = remember { SnackbarHostState() }
		val permissionDeniedMessage = stringResource(Res.string.location_permission_denied)

		BindEffect(controller)

		LaunchedEffect(Unit) {
			coroutineScope.launch {
				try {
					val state = controller.getPermissionState(Permission.LOCATION)
					if (state != PermissionState.Granted) {
						controller.providePermission(Permission.LOCATION)
					}

					if (controller.getPermissionState(Permission.LOCATION) == PermissionState.Granted) {
						val location = getPlatform().locationService.getCurrentLocation()
						location?.let {
							cameraPositionState.animate(
								CameraUpdateFactory.newLatLngZoom(it, 15f)
							)
						}
					} else {
						snackbarHostState.showSnackbar(permissionDeniedMessage)
					}
				} catch (e: Exception) {
					// Permission denied or other error
					snackbarHostState.showSnackbar(permissionDeniedMessage)
				}
			}
		}

		val isDarkTheme = isSystemInDarkTheme()
		var mapStyle by remember { mutableStateOf<MapStyleOptions?>(null) }

		LaunchedEffect(isDarkTheme) {
			val fileName = if (isDarkTheme) "files/mapThemeDark.json" else "files/mapThemeLight.json"
			try {
				val json = Res.readBytes(fileName).decodeToString()
				mapStyle = MapStyleOptions.fromJson(json)
			} catch (e: Exception) {
				// style loading failed
			}
		}

		Box(modifier = Modifier.fillMaxSize()) {
			GoogleMap(
				modifier = Modifier.fillMaxSize(),
				cameraPositionState = cameraPositionState,
				properties = MapProperties(
					mapStyleOptions = mapStyle
				)
			)

			SnackbarHost(
				hostState = snackbarHostState,
				modifier = Modifier.align(Alignment.BottomCenter)
			)

			searchBar()
		}
	}
}
