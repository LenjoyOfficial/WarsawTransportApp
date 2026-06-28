package me.lenjoy.warsawtransportapp.i18n

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
actual fun LanguageProvider(lang: String, content: @Composable () -> Unit) {
	val context = LocalContext.current
	val currentConfiguration = LocalConfiguration.current

	val targetLocale = Locale.forLanguageTag(lang)
	Locale.setDefault(targetLocale)

	val configuration = Configuration(currentConfiguration).apply {
		setLocale(targetLocale)
	}

	val newContext = context.createConfigurationContext(configuration)

	CompositionLocalProvider(
		LocalContext provides newContext,
		LocalConfiguration provides configuration
	) {
		key(lang) {
			content()
		}
	}
}