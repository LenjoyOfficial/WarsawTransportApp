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

interface WarsawTransportApi {
    suspend fun getRoutes(): RoutesResponseDto
    suspend fun getStopLocations(): List<ValuesRowDto>
    suspend fun getStopLines(stopGroupId: String, stopPoleNumber: String): List<ValuesRowDto>
    suspend fun getDepartures(stopGroupId: String, stopPoleNumber: String, line: String): List<List<KeyValueDto>>
}

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
            setBody("{\"busstopId\": \"$stopGroupId\", \"busstopNr\": \"$stopPoleNumber\"}")
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
            setBody("{\"busstopId\": \"$stopGroupId\", \"busstopNr\": \"$stopPoleNumber\", \"line\": \"$line\"}")
        }.body<List<List<KeyValueDto>>>()
        return response
    }
}
