package me.lenjoy.warsawtransportapp.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import eu.buney.maps.CameraPosition
import eu.buney.maps.GoogleMap
import eu.buney.maps.LatLng
import eu.buney.maps.MapProperties
import eu.buney.maps.MapStyleOptions
import eu.buney.maps.MapUiSettings
import eu.buney.maps.rememberBitmapDescriptor
import eu.buney.maps.rememberCameraPositionState
import eu.buney.maps.utils.clustering.Clustering
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import me.lenjoy.warsawtransportapp.ScreenEntry
import me.lenjoy.warsawtransportapp.SettingsScreenEntry
import me.lenjoy.warsawtransportapp.StopDetailScreenEntry
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.Res
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.bus
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.location_permission_denied
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.tram
import me.lenjoy.warsawtransportapp.getPlatform
import me.lenjoy.warsawtransportapp.repository.FavoritesRepository
import me.lenjoy.warsawtransportapp.ui.theme.isDarkTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun HomeScreen(
	stops: List<StopLocation>,
	favoritesRepository: FavoritesRepository,
	searchBar: @Composable () -> Unit,
	onOpenScreen: (ScreenEntry) -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val sheetState = rememberBottomSheetScaffoldState()

	// Bottom sheet content

	BottomSheetScaffold(
		sheetPeekHeight = 110.dp,
		scaffoldState = sheetState,
		sheetContent = {
			val pagerState = rememberPagerState(pageCount = { 2 })

			Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
				SecondaryTabRow(
					selectedTabIndex = pagerState.currentPage,
					modifier = Modifier.fillMaxWidth(),
					containerColor = Color.Transparent
				) {
					Tab(
						selected = pagerState.currentPage == 0,
						onClick = {
							coroutineScope.launch { sheetState.bottomSheetState.expand() }
							coroutineScope.launch { pagerState.animateScrollToPage(0) }
						},
						text = { Text("Explore") }
					)
					Tab(
						selected = pagerState.currentPage == 1,
						onClick = {
							coroutineScope.launch { sheetState.bottomSheetState.expand() }
							coroutineScope.launch { pagerState.animateScrollToPage(1) }
						},
						text = { Text("Favorites") }
					)
				}

				HorizontalPager(
					state = pagerState,
					modifier = Modifier.weight(1f)
				) { page ->
					when (page) {
						0 -> {
							ExploreTab()
						}
						1 -> {
							val favorites = remember { favoritesRepository.getFavorites() }

							FavoriteStopsTab(favorites, onOpenScreen)
						}
					}
				}

				Box(
					modifier = Modifier.fillMaxWidth()
				) {
					Card(
						modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(/*0.33f*/).padding(8.dp),
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
		}
	) { paddingValues ->

		// Set up map

		val warsaw = LatLng(52.237049, 21.017532)
		val cameraPositionState = rememberCameraPositionState {
			position = CameraPosition(target = warsaw, zoom = 12f)
		}

		val factory = rememberPermissionsControllerFactory()
		val controller = remember(factory) { factory.createPermissionsController() }
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
//							cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
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

		val isDarkTheme = isDarkTheme()
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

		val clusterItems = remember(stops) {
			stops.mapNotNull { stop -> if (stop.latitude != null && stop.longitude != null) StopClusterItem(stop) else null }
		}

		var visibleItems by remember { mutableStateOf(clusterItems) }

		LaunchedEffect(clusterItems) {
			snapshotFlow { cameraPositionState.isMoving }
				.collect { isMoving ->
					if (!isMoving) {
						val bounds = cameraPositionState.projection?.visibleBounds
						visibleItems = if (bounds != null) {
							clusterItems.filter { item ->
								val lat = item.position.latitude
								val lng = item.position.longitude
								lat >= bounds.southwest.latitude &&
										lat <= bounds.northeast.latitude &&
										lng >= bounds.southwest.longitude &&
										lng <= bounds.northeast.longitude
							}
						} else {
							clusterItems
						}
					}
				}
		}
		println("Visible items: ${visibleItems.size}")

		val layoutDirection = LocalLayoutDirection.current

		Box(
			modifier = Modifier.fillMaxSize()
		) {
			val now = Clock.System.now()
			GoogleMap(
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(
					top = paddingValues.calculateTopPadding() + 100.dp,
					bottom = paddingValues.calculateBottomPadding(),
					start = paddingValues.calculateStartPadding(layoutDirection),
					end = paddingValues.calculateEndPadding(layoutDirection)
				),
				cameraPositionState = cameraPositionState,
				uiSettings = MapUiSettings(
					mapToolbarEnabled = false,
					tiltGesturesEnabled = false
				),
				properties = MapProperties(
					isMyLocationEnabled = true,
					mapStyleOptions = mapStyle
				)
			) {
				val busIcon = rememberBitmapDescriptor(Res.drawable.bus)
				val tramIcon = rememberBitmapDescriptor(Res.drawable.tram)

				Clustering(
					items = visibleItems,
					/*clusterItemContent = { item ->
						Marker(
							icon = if (item.stop.type == TransportType.Bus) busIcon else tramIcon,
							contentDescription = item.stop.stopName,
						)
					}*/
				)
			}

			val diff = Clock.System.now() - now
			println("Displayed map in ${diff.inWholeMilliseconds} ms")

			val zoom = cameraPositionState.position.zoom
			val zoomInt = (zoom * 10).toInt()
			val zoomText = "${zoomInt / 10}.${abs(zoomInt) % 10}"

			/*Box(
				modifier = Modifier
					.align(Alignment.Center)
					.padding(8.dp)
					.clip(RoundedCornerShape(4.dp))
					.background(Color(0xBB000000))
					.padding(horizontal = 6.dp, vertical = 2.dp)
			) {
				Text(
					text = zoomText,
					color = Color.White,
					style = MaterialTheme.typography.labelSmall
				)
			}*/

			SnackbarHost(
				hostState = snackbarHostState,
				modifier = Modifier.align(Alignment.BottomCenter)
			)

			Box(
				modifier = Modifier.fillMaxWidth()
			) {
				searchBar()
			}
		}
	}
}

@Composable
fun ExploreTab() {
	Box(modifier = Modifier.fillMaxSize()) {
		Text("Explore Content", modifier = Modifier.align(Alignment.Center))
	}
}

@Composable
fun FavoriteStopsTab(
	favorites: List<StopLocation>,
	onOpenScreen: (ScreenEntry) -> Unit
) {
	if (favorites.isEmpty()) {
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Text("No favorites yet")
		}
	} else {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			items(favorites) { stop ->
				Card(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						onOpenScreen(StopDetailScreenEntry(stop))
					}
				) {
					Column(modifier = Modifier.padding(16.dp)) {
						Text(stop.stopName, style = MaterialTheme.typography.titleMedium)
						Text("${stop.stopGroupId.value} ${stop.stopPoleNumber.value}", style = MaterialTheme.typography.bodySmall)
					}
				}
			}
		}
	}
}