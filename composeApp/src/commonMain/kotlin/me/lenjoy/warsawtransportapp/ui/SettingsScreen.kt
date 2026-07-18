package me.lenjoy.warsawtransportapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lenjoy.warsawtransportapp.SettingsScreenEntry
import me.lenjoy.warsawtransportapp.language
import me.lenjoy.warsawtransportapp.themeConfig
import me.lenjoy.warsawtransportapp.ui.theme.ThemeConfig
import org.jetbrains.compose.resources.stringResource
import warsawtransportapp.composeapp.generated.resources.Res
import warsawtransportapp.composeapp.generated.resources.settings_appearance
import warsawtransportapp.composeapp.generated.resources.settings_appearance_desc
import warsawtransportapp.composeapp.generated.resources.settings_language
import warsawtransportapp.composeapp.generated.resources.settings_language_desc
import warsawtransportapp.composeapp.generated.resources.theme_dark
import warsawtransportapp.composeapp.generated.resources.theme_light
import warsawtransportapp.composeapp.generated.resources.theme_system

@Composable
fun SettingsScreen(
	onBack: () -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(SettingsScreenEntry.name)) },
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surfaceVariant
				),
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
					}
				}
			)
		}
	) { paddingValues ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
				.padding(16.dp),
			contentAlignment = Alignment.Center
		) {
			Column(
				modifier = Modifier.fillMaxSize()
			) {
				// Appearance Section
				SettingTitle(stringResource(Res.string.settings_appearance))
				SettingDescription(stringResource(Res.string.settings_appearance_desc))

				Surface(
					color = MaterialTheme.colorScheme.surfaceVariant,
					shape = RoundedCornerShape(12.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					Column {
						ThemeItem(
							icon = Icons.Default.LightMode,
							name = stringResource(Res.string.theme_light),
							selected = themeConfig == ThemeConfig.LIGHT,
							onClick = { themeConfig = ThemeConfig.LIGHT }
						)
						OptionDivider()
						ThemeItem(
							icon = Icons.Default.DarkMode,
							name = stringResource(Res.string.theme_dark),
							selected = themeConfig == ThemeConfig.DARK,
							onClick = { themeConfig = ThemeConfig.DARK }
						)
						OptionDivider()
						ThemeItem(
							icon = Icons.Default.SettingsSuggest,
							name = stringResource(Res.string.theme_system),
							selected = themeConfig == ThemeConfig.SYSTEM,
							onClick = { themeConfig = ThemeConfig.SYSTEM }
						)
					}
				}

				Spacer(modifier = Modifier.padding(top = 24.dp))

				// Language Section
				SettingTitle(stringResource(Res.string.settings_language))
				SettingDescription(stringResource(Res.string.settings_language_desc))

				Surface(
					color = MaterialTheme.colorScheme.surfaceVariant,
					shape = RoundedCornerShape(12.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					Column {
						LanguageItem(
							flag = "🇬🇧",
							name = "English",
							selected = language == "en",
							onClick = { language = "en" }
						)
						OptionDivider()
						LanguageItem(
							flag = "🇵🇱",
							name = "Polski",
							selected = language == "pl",
							onClick = { language = "pl" }
						)
					}
				}
			}
		}
	}
}

@Composable
fun SettingTitle(
	text: String
) {
	Text(
		text = text,
		style = MaterialTheme.typography.titleMedium,
		fontWeight = FontWeight.Bold,
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier.padding(bottom = 8.dp)
	)
}

@Composable
fun SettingDescription(
	text: String
) {
	Text(
		text = text,
		style = MaterialTheme.typography.bodyMedium,
		color = MaterialTheme.colorScheme.onSurfaceVariant,
		modifier = Modifier.padding(bottom = 16.dp)
	)
}

@Composable
fun OptionDivider() {
	HorizontalDivider(
		modifier = Modifier.padding(horizontal = 20.dp),
		color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
	)
}

@Composable
fun ThemeItem(
	icon: ImageVector,
	name: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = name,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.weight(1f)
		)
		RadioButton(
			selected = selected,
			onClick = null,
			colors = RadioButtonDefaults.colors(
				selectedColor = Color(0xFFF48FB1), // Maintaining user preference for pink
				unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
			)
		)
	}
}

@Composable
fun LanguageItem(
	flag: String,
	name: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(text = flag, fontSize = 24.sp)
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = name,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.weight(1f)
		)
		RadioButton(
			selected = selected,
			onClick = null,
			colors = RadioButtonDefaults.colors(
				selectedColor = Color(0xFFF48FB1),
				unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
			)
		)
	}
}
