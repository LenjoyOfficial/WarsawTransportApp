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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.lenjoy.warsawtransportapp.i18n.LanguageProvider
import me.lenjoy.warsawtransportapp.ui.FavouritesScreen
import me.lenjoy.warsawtransportapp.ui.MapScreen
import me.lenjoy.warsawtransportapp.ui.SearchScreen
import me.lenjoy.warsawtransportapp.ui.SettingsScreen
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
}

var language by mutableStateOf("en") // "en" or "pl"
var themeConfig by mutableStateOf(ThemeConfig.SYSTEM)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
	var currentScreen by remember { mutableStateOf<Screen>(Screen.Search) }
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
					TopAppBar(
						title = { Text(stringResource(currentScreen.name)) },
						colors = TopAppBarDefaults.topAppBarColors(
							containerColor = MaterialTheme.colorScheme.surfaceVariant
						)
					)
				},
				bottomBar = {
					NavigationBar {
						screens.forEach { screen ->
							NavigationBarItem(
								icon = { Icon(screen.icon, contentDescription = null) },
								label = { Text(stringResource(screen.name)) },
								selected = currentScreen == screen,
								onClick = { currentScreen = screen }
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
					when (currentScreen) {
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
					}
				}
			}
		}
	}
}
