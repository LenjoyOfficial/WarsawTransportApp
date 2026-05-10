package me.lenjoy.warsawtransportapp.network

import android.R.attr.configure
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    configure()
}

