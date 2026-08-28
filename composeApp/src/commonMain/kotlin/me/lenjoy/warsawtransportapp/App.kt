package me.lenjoy.warsawtransportapp

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.async
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.Res
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.error_fetching_data
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.nav_search
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.nav_settings
import me.lenjoy.warsawtransportapp.i18n.LanguageProvider
import me.lenjoy.warsawtransportapp.repository.FavoritesRepositoryImpl
import me.lenjoy.warsawtransportapp.repository.TransportRepositoryImpl
import me.lenjoy.warsawtransportapp.ui.SettingsScreen
import me.lenjoy.warsawtransportapp.ui.details.RouteDetailScreen
import me.lenjoy.warsawtransportapp.ui.details.StopDetailScreen
import me.lenjoy.warsawtransportapp.ui.search.GlobalSearchBar
import me.lenjoy.warsawtransportapp.ui.search.HomeScreen
import me.lenjoy.warsawtransportapp.ui.theme.AppTheme
import me.lenjoy.warsawtransportapp.ui.theme.ThemeConfig
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// -----------------------------------------------------------------------------------
// Screens
// -----------------------------------------------------------------------------------

sealed interface ScreenEntry

sealed interface RootScreenEntry : ScreenEntry {
	val name: StringResource
	val icon: ImageVector
}

data object HomeScreenEntry : RootScreenEntry {
	override val name = Res.string.nav_search
	override val icon = Icons.Default.Search
}

data object SettingsScreenEntry : RootScreenEntry {
	override val name = Res.string.nav_settings
	override val icon = Icons.Default.Settings
}

data class StopDetailScreenEntry(val stop: StopLocation) : ScreenEntry

data class RouteDetailScreenEntry(val line: String, val routeName: String) : ScreenEntry

// -----------------------------------------------------------------------------------
// App root
// -----------------------------------------------------------------------------------

var language by mutableStateOf("en") // "en" or "pl"
var themeConfig by mutableStateOf(ThemeConfig.SYSTEM)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
	val api = remember { TransportRepositoryImpl() }
	val favoritesRepository = remember { FavoritesRepositoryImpl() }
	var isLoading by remember { mutableStateOf(true) }
	var stops by remember { mutableStateOf<List<StopLocation>>(emptyList()) }
	val snackbarHostState = remember { SnackbarHostState() }
	val errorMessage = stringResource(Res.string.error_fetching_data)

	LaunchedEffect(Unit) {
		try {
			val stopsDeffered = async { api.getAllStops() }
			stops = stopsDeffered.await()
		} catch (e: Exception) {
			snackbarHostState.showSnackbar(errorMessage)
		} finally {
			isLoading = false
		}
	}

	val backStack = remember { mutableStateListOf<ScreenEntry>(HomeScreenEntry) }

	@Composable
	fun RootScreen(
		showPadding: Boolean = true,
		content: @Composable (PaddingValues) -> Unit
	) {
		Scaffold(
			topBar = {
				val last = backStack.last()

				if (last is RootScreenEntry)
					TopAppBar(
						title = { Text(stringResource(last.name)) },
						colors = TopAppBarDefaults.topAppBarColors(
							containerColor = MaterialTheme.colorScheme.surfaceVariant
						),
						navigationIcon = {
							IconButton(onClick = { backStack.removeLastOrNull() }) {
								Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
							}
						}
					)
			},
			snackbarHost = { SnackbarHost(snackbarHostState) }
		) { paddingValues ->
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues)
					.then(if (showPadding) Modifier.padding(16.dp) else Modifier),
				contentAlignment = Alignment.Center
			) {
				content(paddingValues)
			}
		}
	}

	LanguageProvider(lang = language) {
		AppTheme(themeConfig = themeConfig) {
			Box(
				modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
			) {
				NavDisplay(
					backStack = backStack,
					transitionSpec = {
						slideInHorizontally { it } togetherWith fadeOut()
					},
					popTransitionSpec = {
						fadeIn() togetherWith slideOutHorizontally { it }
					},
					predictivePopTransitionSpec = {
						fadeIn() togetherWith slideOutHorizontally { it }
					},
					entryProvider = entryProvider {
						entry<HomeScreenEntry> {
							HomeScreen(
								stops = stops,
								favoritesRepository = favoritesRepository,
								searchBar = {
									GlobalSearchBar(isLoading, stops) { stop ->
										backStack.add(StopDetailScreenEntry(stop))
									}
								},
								onOpenScreen = {
									backStack.add(it)
								}
							)
						}
						entry<SettingsScreenEntry> {
							SettingsScreen(
								onBack = { backStack.removeLast() }
							)
						}
						entry<StopDetailScreenEntry> { screen ->
							val stop = screen.stop

							StopDetailScreen(
								stop = stop,
								repository = api,
								favoritesRepository = favoritesRepository,
								onBack = { backStack.removeLast() },
								onDepartureClick = { departure ->
									departure.route?.let { route ->
										backStack.add(RouteDetailScreenEntry(departure.line.value, route))
									}
								}
							)
						}
						entry<RouteDetailScreenEntry> { screen ->
							RouteDetailScreen(
								line = screen.line,
								routeName = screen.routeName,
								repository = api,
								onBack = { backStack.removeLast() }
							)
						}
					},
				)
			}
		}
	}
}
