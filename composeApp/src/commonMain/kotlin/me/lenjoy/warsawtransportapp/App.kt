package me.lenjoy.warsawtransportapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.i18n.LanguageProvider
import me.lenjoy.warsawtransportapp.repository.TransportRepositoryImpl
import me.lenjoy.warsawtransportapp.ui.FavouritesScreen
import me.lenjoy.warsawtransportapp.ui.MapScreen
import me.lenjoy.warsawtransportapp.ui.SettingsScreen
import me.lenjoy.warsawtransportapp.ui.details.StopDetailScreen
import me.lenjoy.warsawtransportapp.ui.search.GlobalSearchBar
import me.lenjoy.warsawtransportapp.ui.search.SearchScreen
import me.lenjoy.warsawtransportapp.ui.theme.AppTheme
import me.lenjoy.warsawtransportapp.ui.theme.ThemeConfig
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import warsawtransportapp.composeapp.generated.resources.Res
import warsawtransportapp.composeapp.generated.resources.nav_favorites
import warsawtransportapp.composeapp.generated.resources.nav_map
import warsawtransportapp.composeapp.generated.resources.nav_search
import warsawtransportapp.composeapp.generated.resources.nav_settings

sealed class Screen(val name: StringResource, val icon: ImageVector) {
	object Search : Screen(Res.string.nav_search, Icons.Default.Search)
	object Favourites : Screen(Res.string.nav_favorites, Icons.Default.Favorite)
	object Map : Screen(Res.string.nav_map, Icons.Default.Map)
	object Settings : Screen(Res.string.nav_settings, Icons.Default.Settings)
	data class StopDetail(val stop: StopLocation) : Screen(Res.string.nav_search, Icons.Default.Search)
}

@Serializable
sealed interface Route {
	@Serializable
	object Search : Route

	@Serializable
	object Favourites : Route

	@Serializable
	object Map : Route

	@Serializable
	object Settings : Route

	@Serializable
	data class StopDetail(
		val stopGroupId: String,
		val stopPoleNumber: String,
		val stopName: String
	) : Route
}

var language by mutableStateOf("en") // "en" or "pl"
var themeConfig by mutableStateOf(ThemeConfig.SYSTEM)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
	val api = remember { TransportRepositoryImpl() }
	var isLoading by remember { mutableStateOf(true) }
	var stops by remember { mutableStateOf<List<StopLocation>>(emptyList()) }

	LaunchedEffect(Unit) {
		val routesDeffered = async { api.getRoutes() }
		val stopsDeffered = async { api.getAllStops() }
		routesDeffered.await()
		stops = stopsDeffered.await()
		isLoading = false
	}

	val backStack = remember { mutableStateListOf<Screen>(Screen.Search) }
	val screens = listOf(
		Screen.Search,
		Screen.Favourites,
		Screen.Map,
		Screen.Settings
	)

	LanguageProvider(lang = language) {
		AppTheme(themeConfig = themeConfig) {
			Scaffold(
				topBar = {
					if (backStack.last() == Screen.Settings)
						TopAppBar(
							title = { Text(stringResource(backStack.last().name)) },
							colors = TopAppBarDefaults.topAppBarColors(
								containerColor = MaterialTheme.colorScheme.surfaceVariant
							)
						)
					else {
						GlobalSearchBar(isLoading, stops) { stop ->
							backStack.add(Screen.StopDetail(stop))
						}
					}
				},
				bottomBar = {
					NavigationBar {
						screens.forEach { screen ->
							NavigationBarItem(
								icon = { Icon(screen.icon, contentDescription = null) },
								label = { Text(stringResource(screen.name)) },
								selected = backStack.last() == screen,
								onClick = { backStack.set(0, screen) }
							)
						}
					}
				}
			) { paddingValues ->
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues)
				) {
					NavDisplay(
						backStack = backStack,
						entryProvider = { screen ->
							when (screen) {
								Screen.Search -> NavEntry(screen) { SearchScreen() }
								Screen.Favourites -> NavEntry(screen) { FavouritesScreen() }
								Screen.Map -> NavEntry(screen) { MapScreen() }
								Screen.Settings -> NavEntry(screen) { SettingsScreen() }
								is Screen.StopDetail -> NavEntry(screen) {
									val stop = screen.stop
									StopDetailScreen(
										stopGroupId = stop.stopGroupId.value,
										stopPoleNumber = stop.stopPoleNumber.value,
										stopName = stop.stopName,
										repository = api,
										onBack = { }
									)
								}
							}
						},
					)
					/*when (currentScreen) {
						Screen.Search -> SearchScreen(
							modifier = Modifier
								.fillMaxSize()
								.padding(16.dp)
						)
						Screen.Favourites -> FavouritesScreen(
							modifier = Modifier
								.fillMaxSize()
								.padding(16.dp)
						)
						Screen.Map -> MapScreen(
							modifier = Modifier
								.fillMaxSize()
								.padding(16.dp)
						)
						Screen.Settings -> SettingsScreen(
							modifier = Modifier
								.fillMaxSize()
								.padding(16.dp)
						)
						is Screen.StopDetail -> {
							val stop = (currentScreen as Screen.StopDetail).stop
							StopDetailScreen(
								stopGroupId = stop.stopGroupId.value,
								stopPoleNumber = stop.stopPoleNumber.value,
								stopName = stop.stopName,
								repository = api,
								onBack = { currentScreen = Screen.Search }
							)
						}
					}*/
				}
			}
		}
	}
}
