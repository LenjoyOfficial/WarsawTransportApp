package me.lenjoy.warsawtransportapp.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import me.lenjoy.warsawtransportapp.api.dto.DepartureDto
import me.lenjoy.warsawtransportapp.api.dto.RouteResponseDto
import me.lenjoy.warsawtransportapp.api.dto.StopLinesResponseDto
import me.lenjoy.warsawtransportapp.api.dto.StopLocationDto
import me.lenjoy.warsawtransportapp.config.BuildKonfig
import me.lenjoy.warsawtransportapp.network.createHttpClient

interface WarsawTransportApi {
	suspend fun getRoutes(line: String): RouteResponseDto

	suspend fun getStopLocations(): List<StopLocationDto>

	suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): StopLinesResponseDto

	suspend fun getDepartures(stopGroupId: String, stopPoleNumber: String, line: String): List<DepartureDto>
}

class WarsawTransportApiImpl(
	private val client: HttpClient = createHttpClient(),
) : WarsawTransportApi {
	private val baseUrl = "https://wt-functions-hwecdjb5bth6fjf8.polandcentral-01.azurewebsites.net/api"
	private val apiKey = BuildKonfig.ZTM_API_KEY

	override suspend fun getRoutes(line: String): RouteResponseDto {
		val response = client.get("$baseUrl/routes/$line") {
			header("x-functions-key", apiKey)
		}.body<RouteResponseDto>()
		return response
	}

	override suspend fun getStopLocations(): List<StopLocationDto> {
		val response = client.get("$baseUrl/stops") {
			header("x-functions-key", apiKey)
		}.body<List<StopLocationDto>>()
		return response
	}

	override suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): StopLinesResponseDto {
		val response = client.get("$baseUrl/stops/$stopGroupId/$stopPoleNumber/lines") {
			header("x-functions-key", apiKey)
		}.body<StopLinesResponseDto>()
		return response
	}

	override suspend fun getDepartures(
		stopGroupId: String,
		stopPoleNumber: String,
		line: String,
	): List<DepartureDto> {
		val response = client.get("$baseUrl/stops/$stopGroupId/$stopPoleNumber/lines/$line/departures") {
			header("x-functions-key", apiKey)
		}.body<List<DepartureDto>>()
		return response
	}
}
