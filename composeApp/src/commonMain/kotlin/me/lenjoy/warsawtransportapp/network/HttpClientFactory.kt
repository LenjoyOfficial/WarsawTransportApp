package me.lenjoy.warsawtransportapp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun io.ktor.client.HttpClientConfig<*>.configure() {
	install(ContentNegotiation) {
		json(
			Json {
				ignoreUnknownKeys = true
				isLenient = true
			}
		)
	}
	install(Logging) {
		level = LogLevel.INFO
	}
	install(HttpTimeout) {
		requestTimeoutMillis = 15_000
		connectTimeoutMillis = 15_000
		socketTimeoutMillis = 15_000
	}
	defaultRequest {
		header(HttpHeaders.Accept, ContentType.Application.Json)
	}
}

expect fun createHttpClient(): HttpClient

