package me.lenjoy.warsawtransportapp.i18n

import androidx.compose.runtime.Composable

@Composable
expect fun LanguageProvider(lang: String, content: @Composable () -> Unit)
