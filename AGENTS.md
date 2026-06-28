# Project: WarsawTransportApp

## Overview
WarsawTransportApp is a Kotlin Multiplatform (KMP) application for real-time and scheduled public transport info in Warsaw. Shared logic for networking, parsing, and caching.

## Tech Stack
- **Language**: Kotlin 2.1.0
- **UI**: Compose Multiplatform
- **Network**: Ktor (Darwin/OkHttp)
- **Serialization**: Kotlinx Serialization
- **FS/Cache**: Okio
- **Date/Time**: Kotlinx Datetime

## Project Structure
- `commonMain`: 
    - `api/`: Transport API, DTOs, parsers.
    - `api/cache/`: Disk caching layer.
    - `network/`: Ktor config + keys.
    - `repository/`: Data access.
- `androidMain` / `iosMain`: Platform implementations.
- `iosApp/`: SwiftUI entry point.

## Documentation
- `docs/DATA_MODEL.md`: Domain models and data structures.
- `docs/API_DOCUMENTATION.md`: ZTM Warsaw API details.

## Goals
1. **KMP Efficiency**: Maximize shared code.
2. **Offline-First**: Disk cache for API data.
3. **Accuracy**: Real-time ZTM Warsaw data.
4. **Clean Arch**: Separate API, domain, and UI.

## Agent Guidelines
- **KMP First**: Logic → `commonMain`.
- **Cache**: Wrap new API calls in `CachedWarsawTransportApi`.
- **Consistency**: Follow DTO-to-Model mapping patterns.
