# Data Model

## Overview
- **DTOs** = wire shapes from the internal backend API
- **Domain models** = app-ready types
- **Parsers** = DTO → domain converters
- **Cache** = Persistent disk storage for API responses

---

## DTOs (Wire Format)

### `api.dto.TransportDtos`

```kotlin
// Vehicle (real-time positions) — kept from old ZTM API
VehicleDto {
  lines, lon, lat, vehicleNumber, time, brigade
}

// Route response (GET /api/routes/{n})
RouteResponseDto {
  routeNumber: String,
  transportType: String,          // "tram" | "bus" | "train" | "unknown"
  variants: Map<String, RouteVariantDto>
}

RouteVariantDto {
  name: String,
  stops: Map<String, RouteStopDto>   // key = stop sequence number
}

RouteStopDto {
  streetId: String?,
  stopGroup: String?,                 // "stop_group"
  stopNumber: String?,                // "stop_number"
  type: String?,
  distance: Int?
}

// Stop locations (GET /api/stops)
StopLocationDto {
  stopGroup: String,          // "stop_group"
  stopPole: String,           // "stop_pole"
  stopGroupName: String,      // "stop_group_name"
  streetId: String?,
  latitude: Double?,
  longitude: Double?,
  direction: String?,
  validFrom: String?          // "valid_from"
}

// Lines at stop (GET /api/stops/{id}/{pole}/lines)
StopLinesResponseDto {
  stopId: String, stopNumber: String,
  lines: List<String>
}

// Departures (GET /api/stops/{id}/{pole}/lines/{line}/departures)
DepartureDto {
  departureTime: String,       // "departure_time" HH:mm:ss
  brigade: String?,
  direction: String?,
  route: String?,
  symbol1: String?, symbol2: String?
}
```

---

## Domain Models (App Types)

### `api.model.TransportModels`

```kotlin
// Value types (type-safe)
StopGroupId(value: String)
StopPoleNumber(value: String)
LineNumber(value: String)

// Vehicle
Vehicle {
  line: LineNumber,
  latitude: Double, longitude: Double,
  vehicleNumber: String,
  time: String,
  brigade: String
}

// Stop location
StopLocation {
  stopGroupId: StopGroupId,
  stopPoleNumber: StopPoleNumber,
  stopName: String,
  streetId: String?,
  latitude: Double?, longitude: Double?,
  direction: String?,
  validFrom: String?
}

// Lines at a stop
StopLine { line: LineNumber }

// Departure
Departure {
  line: LineNumber,
  route: String?,
  direction: String?,
  serviceTime: ServiceTime,
  brigade: String?,
  symbol1: String?, symbol2: String?
}

// Service time (handles 24+ hours for night lines)
ServiceTime {
  raw: String,           // "26:15:00"
  dayOffset: Int,
  hourOfDay: Int,
  minute: Int,
  second: Int
}
```

### `api.model.RouteModels`

```kotlin
TransportType { Tram, Bus, Train, Unknown }

RouteLine {
  line: String,
  routeName: String,
  transportType: TransportType,
  stops: List<RouteStop>
}

RouteStop {
  sequence: Int,
  streetId: String?,
  stopGroupId: String,
  stopPoleNumber: String,
  type: String?,
  distanceMeters: Int?
}
```

---

## Parsers

### `api.parser.RouteParsers`
Convert DTOs → domain models.

```kotlin
parseServiceTime(raw: String): ServiceTime?
  // "26:15:00" → ServiceTime(dayOffset=1, hourOfDay=2, minute=15, ...)

parseStopLocation(dto: StopLocationDto): StopLocation
  // Direct mapping; null-safe fields become null in domain model

parseStopLines(response: StopLinesResponseDto): List<StopLine>
  // Maps response.lines → List<StopLine>

parseDepartures(rows: List<DepartureDto>, line: String): List<Departure>
  // Parses departure_time via parseServiceTime; sets line from parameter

parseVehicleDto(...): Vehicle?
```

### `api.parser.RouteResponseParser`
Parse route DTO.

```kotlin
parseRouteLines(response: RouteResponseDto): List<RouteLine>
  // Extracts transport_type → TransportType enum
  // Flattens variants → List<RouteLine>, each with stops from variant.stops
```

---

## Caching System

Persistent disk caching layer with a 24-hour expiration policy.

### `api.cache.CacheManager`
Handles cross-platform file persistence using **Okio**.
- Stores entries in `api_cache/` directory.
- Each key has two files: `.json` (data) and `.time` (fetch timestamp).
- **TTL**: 24 hours.

```kotlin
CacheManager(fileSystem: FileSystem) {
  fun <T> save(key: String, data: T, serializer: KSerializer<T>)
  fun <T> get(key: String, serializer: KSerializer<T>): T?
}
```

### `api.CachedWarsawTransportApi`
A **Decorator** for `WarsawTransportApi` that intercepts calls to provide cached data.
- **Hit**: Returns data from disk if timestamp is < 24h old.
- **Miss**: Performs network request, saves result to disk, returns result.

---

## API Service (`api.WarsawTransportApi`)

Raw HTTP calls to 4 endpoints. Returns typed DTOs. Uses `GET` with `x-functions-key` header.

```kotlin
interface WarsawTransportApi {
    suspend fun getRoutes(line: String): RouteResponseDto
    suspend fun getStopLocations(): List<StopLocationDto>
    suspend fun getStopLines(stopGroupId, stopPoleNumber): StopLinesResponseDto
    suspend fun getDepartures(stopGroupId, stopPoleNumber, line): List<DepartureDto>
}
```

Implementations:
- `WarsawTransportApiImpl`: The "live" network implementation.
- `CachedWarsawTransportApi`: The caching wrapper (default used by repository).

---

## Repository (`repository.TransportRepository`)

Wraps API + parsers. Returns domain models only. UI layer depends on this, not the API.

```kotlin
interface TransportRepository {
    suspend fun getRoutes(line: String): List<RouteLine>
    suspend fun getAllStops(): List<StopLocation>
    suspend fun getStopLines(stopGroupId, stopPoleNumber): List<StopLine>
    suspend fun getDepartures(stopGroupId, stopPoleNumber, line): List<Departure>
}
```

### `repository.FavoritesRepository`

Handles persistent user favorites.

```kotlin
interface FavoritesRepository {
    fun getFavorites(): List<StopLocation>
    fun toggleFavorite(stop: StopLocation)
    fun isFavorite(stopGroupId: String, stopPoleNumber: String): Boolean
}
```

Used by `TransportRepositoryImpl`:
- Calls API methods (via `CachedWarsawTransportApi`)
- Maps DTOs → domain models
- Ready for ViewModel consumption

---

## Usage Flow (with Cache)

```
UI (ViewModel)
  ↓ calls
Repository.getDepartures(...)
  ↓ calls
CachedApi.getDepartures(...)
  ↓ check disk
[ Disk Hit? ] → return DTO from disk
[ Disk Miss? ]
    ↓ calls network
    ApiImpl.getDepartures(...) → DTO
    ↓ saves to disk
    CacheManager.save(DTO)
    ↓ return DTO
  ↓ parses
parseDepartures(DTO) → List<Departure>
  ↓ returns
Repository → Departure (domain models)
  ↓ consumed
ViewModel → UI
```
