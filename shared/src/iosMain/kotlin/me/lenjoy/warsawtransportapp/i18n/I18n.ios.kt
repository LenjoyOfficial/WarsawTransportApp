package me.lenjoy.warsawtransportapp.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSUserDefaults

private val LocalIosLocale = staticCompositionLocalOf { "en" }

@Composable
actual fun LanguageProvider(lang: String, content: @Composable () -> Unit) {
	// Sync language selection with iOS native defaults
	NSUserDefaults.standardUserDefaults.setObject(arrayListOf(lang), "AppleLanguages")

	CompositionLocalProvider(LocalIosLocale provides lang) {
		key(lang) {
			content()
		}
	}
}