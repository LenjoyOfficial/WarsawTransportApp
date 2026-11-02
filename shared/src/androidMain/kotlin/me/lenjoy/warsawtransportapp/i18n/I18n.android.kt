package me.lenjoy.warsawtransportapp.i18n

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalResources
import java.util.Locale

@Composable
actual fun LanguageProvider(lang: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val currentConfiguration = LocalConfiguration.current

    val targetLocale = Locale.forLanguageTag(lang)
    if (LocalLocale.current.platformLocale != targetLocale)
        Locale.setDefault(targetLocale)

    Locale.setDefault(targetLocale)
    currentConfiguration.setLocale(targetLocale)

    val resources = LocalResources.current
    val wrappedContext = context.createConfigurationContext(currentConfiguration)
    resources.updateConfiguration(currentConfiguration, resources.displayMetrics)

    CompositionLocalProvider(
        LocalConfiguration provides currentConfiguration
    ) {
        key(lang) {
            content()
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    if (this is Activity) return this
    return if (this is ContextWrapper) baseContext.findActivity() else null
}
