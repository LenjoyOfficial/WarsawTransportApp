package me.lenjoy.warsawtransportapp.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import eu.buney.maps.CameraUpdateFactory
import eu.buney.maps.GoogleMap
import eu.buney.maps.LatLng
import eu.buney.maps.rememberCameraPositionState
import kotlinx.coroutines.launch
import me.lenjoy.warsawtransportapp.getPlatform
import org.jetbrains.compose.resources.stringResource
import warsawtransportapp.composeapp.generated.resources.Res
import warsawtransportapp.composeapp.generated.resources.location_permission_denied

@Composable
fun SearchScreen() {
    val warsaw = LatLng(52.237049, 21.017532)
    val cameraPositionState = rememberCameraPositionState {
        position = eu.buney.maps.CameraPosition(target = warsaw, zoom = 12f)
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

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
