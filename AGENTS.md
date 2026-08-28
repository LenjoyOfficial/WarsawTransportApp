# WarsawTransportApp

KMP (Kotlin 2.4.0) Compose Multiplatform app for Warsaw public transport.

## Build & Test

```sh
./gradlew :composeApp:assembleDebug        # Android
./gradlew :composeApp:allTests             # all targets (no device needed)
./gradlew :composeApp:check                # checks + unit tests
```

API keys (`ztm.api.key`, `maps.api.key`) required to compile — set in `local.properties` (Android) or `iosApp/Configuration/Config.xcconfig` (iOS). BuildKonfig codegen generates `me.lenjoy.warsawtransportapp.config.BuildKonfig`.

## Architecture

```
composeApp/src/
  commonMain/kotlin/me/lenjoy/warsawtransportapp/
    api/           — WarsawTransportApi (interface), CachedWarsawTransportApi (decorator), DTOs, parsers
    api/cache/     — CacheManager (Okio, 24h TTL)
    network/       — Ktor HttpClient config (30s timeout, lenient JSON, x-functions-key header)
    repository/    — TransportRepository (via CachedApi), FavoritesRepository
    ui/            — Compose screens (NavDisplay + Navigation3 routing)
    cache/         — expect/actual platform FileSystem + cacheDir
    i18n/          — expect/actual language provider
  androidMain/     — OkHttp engine, Android FileSystem/cacheDir
  iosMain/         — Darwin engine, iOS FileSystem/cacheDir
  commonTest/      — kotlin.test, Okio FakeFileSystem
```

## Key Conventions

- **Indentation**: tabs
- **Cache wrapper**: always use `CachedWarsawTransportApi` (default in `TransportRepositoryImpl`)
- **DTO → Model**: parsers in `api/parser/` convert DTOs to domain models
- **ServiceTime**: handles night-line 24h+ times (e.g. `"26:15:00"` → dayOffset=1, hourOfDay=2)
- **Platform expect/actual**: `FileSystem`, `HttpClient`, `LocationService`, `I18n` — add new `expect` declarations for platform-specific code
- **Compose Resources**: generated `Res` object for strings/icons (do not hardcode string resources)

## Stack

- Kotlin 2.4.0, AGP 9.3.0, Compose Multiplatform 1.11.1
- Ktor 3.5.1, Kotlinx Serialization, Kotlinx Datetime 0.8.0
- Okio 3.17.0 (cache), Navigation3, moko-permissions, kmp-maps-compose
- compileSdk/targetSdk 37, minSdk 31, JVM 17

## Docs

- `docs/API_DOCUMENTATION.md` — Azure Functions backend endpoints
- `docs/DATA_MODEL.md` — DTOs, domain models, parsers, caching flow
