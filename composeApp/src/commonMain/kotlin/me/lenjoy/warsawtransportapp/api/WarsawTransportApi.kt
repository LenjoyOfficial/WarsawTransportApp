package me.lenjoy.warsawtransportapp.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import me.lenjoy.warsawtransportapp.api.dto.KeyValueDto
import me.lenjoy.warsawtransportapp.api.dto.RoutesResponseDto
import me.lenjoy.warsawtransportapp.api.dto.ValuesRowDto
import me.lenjoy.warsawtransportapp.network.ApiKeyName
import me.lenjoy.warsawtransportapp.network.ApiKeys
import me.lenjoy.warsawtransportapp.network.createHttpClient

/**
 * Interface defining the available endpoints for the Warsaw Public Transport API.
 * Provides access to routes, stop locations, and real-time departure data.
 */
interface WarsawTransportApi {
	/**
	 * Fetches all available transit routes and their stop sequences.
	 */
	suspend fun getRoutes(): RoutesResponseDto

	/**
	 * Fetches the physical locations and metadata for all transit stops.
	 */
	suspend fun getStopLocations(): List<ValuesRowDto>

	/**
	 * Fetches the list of lines serving a specific stop.
	 *
	 * @param stopGroupId The ID of the stop group (zespół przystankowy).
	 * @param stopPoleNumber The specific pole number within the group.
	 */
	suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): List<ValuesRowDto>

	/**
	 * Fetches scheduled departures for a specific line at a specific stop.
	 *
	 * @param stopGroupId The ID of the stop group.
	 * @param stopPoleNumber The specific pole number.
	 * @param line The line number.
	 */
	suspend fun getDepartures(stopGroupId: String, stopPoleNumber: String, line: String): List<List<KeyValueDto>>
}

/**
 * Real-time network implementation of [WarsawTransportApi] using Ktor.
 */
class WarsawTransportApiImpl(
	private val client: HttpClient = createHttpClient(),
) : WarsawTransportApi {
	private val legacyUrl = "https://api.um.warszawa.pl/api/action"
	private val daneUrl = "https://dane.um.warszawa.pl/api/action"
	private val legacyKey = ApiKeys.get(ApiKeyName.LEGACY)
	private val apiKey = ApiKeys.get(ApiKeyName.ZTM)

	override suspend fun getRoutes(): RoutesResponseDto {
		val response = client.get("$legacyUrl/public_transport_routes/") {
			parameter("apikey", legacyKey)
		}.body<RoutesResponseDto>()
		return response
	}

	override suspend fun getStopLocations(): List<ValuesRowDto> {
		val response = client.post("$daneUrl/get_ztm_przystanki_komunikacji_miejskiej") {
			header(HttpHeaders.Authorization, apiKey)
		}.body<List<ValuesRowDto>>()
		return response
	}

	override suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): List<ValuesRowDto> {
		val response = client.post("$daneUrl/get_ztm_lista_linii_na_przystanku") {
			header(HttpHeaders.Authorization, apiKey)
			header(HttpHeaders.ContentType, ContentType.Application.Json)
			setBody(mapOf("busstopId" to stopGroupId, "busstopNr" to stopPoleNumber))
		}.body<List<ValuesRowDto>>()
		return response
	}

	override suspend fun getDepartures(
		stopGroupId: String,
		stopPoleNumber: String,
		line: String,
	): List<List<KeyValueDto>> {
		val response = client.post("$daneUrl/get_ztm_odjazdy_linii_z_przystanku") {
			header(HttpHeaders.Authorization, apiKey)
			header(HttpHeaders.ContentType, ContentType.Application.Json)
			setBody(mapOf("busstopId" to stopGroupId, "busstopNr" to stopPoleNumber, "line" to line))
		}.body<List<List<KeyValueDto>>>()
		return response
	}
}
