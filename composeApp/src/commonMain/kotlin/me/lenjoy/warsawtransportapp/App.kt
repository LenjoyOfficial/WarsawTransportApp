package me.lenjoy.warsawtransportapp

import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import warsawtransportapp.composeapp.generated.resources.error_fetching_data
import warsawtransportapp.composeapp.generated.resources.nav_favorites
import warsawtransportapp.composeapp.generated.resources.nav_map
import warsawtransportapp.composeapp.generated.resources.nav_search
import warsawtransportapp.composeapp.generated.resources.nav_settings

sealed class Screen(val name: StringResource, val icon: ImageVector) {
    object Search : Screen(Res.string.nav_search, Icons.Default.Search)
    object Favourites : Screen(Res.string.nav_favorites, Icons.Default.Favorite)
    object Map : Screen(Res.string.nav_map, Icons.Default.Map)
    object Settings : Screen(Res.string.nav_settings, Icons.Default.Settings)
    data class StopDetail(val stop: StopLocation) :
        Screen(Res.string.nav_search, Icons.Default.Search)
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
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(Res.string.error_fetching_data)

    LaunchedEffect(Unit) {
        try {
            val routesDeffered = async { api.getRoutes() }
            val stopsDeffered = async { api.getAllStops() }
            routesDeffered.await()
            stops = stopsDeffered.await()
        } catch (e: Exception) {
            snackbarHostState.showSnackbar(errorMessage)
        } finally {
            isLoading = false
        }
    }

    val backStack = remember { mutableStateListOf<Screen>(Screen.Search) }
    val screens = listOf(
        Screen.Search,
        Screen.Favourites,
        Screen.Map,
        Screen.Settings
    )

    @Composable
    fun RootScreen(
        showPadding: Boolean = true,
        content: @Composable (PaddingValues) -> Unit
    ) {
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
                            onClick = { backStack[0] = screen }
                        )
                    }
                }
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
                    popTransitionSpec = {
                        fadeIn() togetherWith slideOutHorizontally { it }
                    },
                    predictivePopTransitionSpec = {
                        fadeIn() togetherWith slideOutHorizontally { it }
                    },
                    entryProvider = { screen ->
                        when (screen) {
                            Screen.Search -> NavEntry(screen) { RootScreen(showPadding = false) { SearchScreen() } }
                            Screen.Favourites -> NavEntry(screen) { RootScreen { FavouritesScreen() } }
                            Screen.Map -> NavEntry(screen) { RootScreen(showPadding = false) { MapScreen() } }
                            Screen.Settings -> NavEntry(screen) { RootScreen { SettingsScreen() } }
                            is Screen.StopDetail -> NavEntry(screen) {
                                val stop = screen.stop
                                StopDetailScreen(
                                    stopGroupId = stop.stopGroupId.value,
                                    stopPoleNumber = stop.stopPoleNumber.value,
                                    stopName = stop.stopName,
                                    repository = api,
                                    onBack = { backStack.removeLast() }
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}
