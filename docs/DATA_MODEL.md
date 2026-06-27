# Data Model

## Overview
- **DTOs** = wire shapes from Warsaw API
- **Domain models** = app-ready types
- **Parsers** = DTO → domain converters
- **Cache** = Persistent disk storage for API responses

---

## DTOs (Wire Format)

### `api.dto.TransportDtos`

```kotlin
// Top-level API response wrapper
ApiResultDto<T> { result: T }

// Vehicle (real-time positions)
VehicleDto {
  lines: String,
  lat: String, lon: String,
  vehicleNumber: String,
  time: String,          // "2026-06-24 14:30:00"
  brigade: String
}

// Stop/Timetable rows (key-value arrays)
KeyValueDto { key: String, value: String? }
ValuesRowDto { values: List<KeyValueDto> }

// Route Stop (nested in routes API)
RouteStopDto {
  ulica_id: String?,
  nr_zespolu: String?,
  nr_przystanku: String?,
  typ: String?,
  odleglosc: Int?
}

// Routes response type alias
typealias RoutesResponseDto = ApiResultDto<Map<String, Map<String, Map<String, RouteStopDto>>>>
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
  time: String,       // raw API time
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
  validFrom: String?  // "2026-01-01 00:00:00"
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
  raw: String,        // "26:15:00"
  dayOffset: Int,     // 0 or 1 (if > 24h)
  hourOfDay: Int,     // 0-23
  minute: Int,
  second: Int
}
```

### `api.model.RouteModels`

```kotlin
RouteLine {
  routeId: String,          // "1"
  routeName: String,        // "TD-3BAN"
  stops: List<RouteStop>
}

RouteStop {
  sequence: Int,
  streetId: String?,
  stopGroupId: String,      // "3240"
  stopPoleNumber: String,   // "04"
  type: String?,
  distanceMeters: Int?
}
```

---

## Parsers

### `api.parser.KeyValueParsers`
Convert `List<KeyValueDto>` ↔ `Map<String, String?>`.

```kotlin
List<KeyValueDto>.asMap(): Map<String, String?>
Map<String, String?>.string(key: String): String?
Map<String, String?>.double(key: String): Double?
Map<String, String?>.int(key: String): Int?
```

### `api.parser.RouteParsers`
Convert DTOs → domain models.

```kotlin
parseServiceTime(raw: String): ServiceTime?
  // "26:15:00" → ServiceTime(dayOffset=1, hourOfDay=2, minute=15, ...)

parseStopLocation(values: List<KeyValueDto>): StopLocation?
parseStopLines(rows: List<ValuesRowDto>): List<StopLine>
parseDepartures(rows: List<List<KeyValueDto>>): List<Departure>
parseVehicleDto(...): Vehicle?
```

### `api.parser.RouteResponseParser`
Parse nested route DTO.

```kotlin
parseRouteLines(response: RoutesResponseDto): List<RouteLine>
  // Flattens route-id -> route-name -> sequence -> stop hierarchy
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

Raw HTTP calls to 4 endpoints. Returns typed DTOs.

```kotlin
interface WarsawTransportApi {
    suspend fun getRoutes(): RoutesResponseDto
    suspend fun getStopLocations(): List<ValuesRowDto>
    suspend fun getStopLines(stopGroupId, stopPoleNumber): List<ValuesRowDto>
    suspend fun getDepartures(stopGroupId, stopPoleNumber, line): List<List<KeyValueDto>>
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
    suspend fun getRoutes(): List<RouteLine>
    suspend fun getAllStops(): List<StopLocation>
    suspend fun getStopLines(stopGroupId, stopPoleNumber): List<StopLine>
    suspend fun getDepartures(stopGroupId, stopPoleNumber, line): List<Departure>
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
