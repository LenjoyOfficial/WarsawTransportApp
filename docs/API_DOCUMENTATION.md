# Internal Backend API Documentation

This document describes the Azure Functions HTTP endpoints exposed by this backend. These endpoints wrap the external ZTM APIs and serve cached or live data.

Base URL: `https://wt-functions-hwecdjb5bth6fjf8.polandcentral-01.azurewebsites.net/`

All requests require the `x-functions-key` header with the Azure Function access key.

---

## 1. Get Route

### Purpose
Returns route data including transport type and stop sequence for a specific route number. Data is sourced from the daily cache in Cosmos DB (synced nightly).

### Endpoint
- **Method:** `GET`
- **URL:** `/api/routes/{route_number}`

### Required headers
- `x-functions-key` — Azure Function access key

### Response format
- JSON

### Response structure
Top-level fields:

- `route_number` — string, route identifier (e.g. `"1"`, `"523"`)
- `transport_type` — string, one of `tram`, `bus`, `train`, `unknown`
- `variants` — object keyed by variant name (e.g. `"TD-3BAN"`)

Each variant contains:

- `name` — string, variant name
- `stops` — object keyed by stop sequence number (string)

Each stop entry contains:

- `street_id` — string, street ID
- `stop_group` — string, stop group number
- `stop_number` — string, stop pole number
- `type` — string, stop type code
- `distance` — number, distance from route start in meters

### Example request
```bash
curl 'https://warsaw-transport-function-hzg8h7buhbepd0aw.polandcentral-01.azurewebsites.net/api/routes/1' \
  --header 'x-functions-key: <FUNCTION_KEY>'
```

### Example response
```json
{
  "route_number": "1",
  "transport_type": "tram",
  "variants": {
    "TD-3BAN": {
      "name": "TD-3BAN",
      "stops": {
        "1": {
          "street_id": "2513",
          "stop_group": "R-03",
          "stop_number": "00",
          "type": "6",
          "distance": 0
        },
        "2": {
          "street_id": "1205",
          "stop_group": "3240",
          "stop_number": "04",
          "type": "5",
          "distance": 245
        }
      }
    }
  }
}
```

### Error responses
- `400` — Missing route number
- `404` — Route not found in cache

---

## 2. Get Stops

### Purpose
Returns all public transport stop locations. Data is sourced from the daily cache in Cosmos DB (synced nightly).

### Endpoint
- **Method:** `GET`
- **URL:** `/api/stops`

### Required headers
- `x-functions-key` — Azure Function access key

### Response format
- JSON (array)

### Response structure
Each stop entry contains:

- `stop_group` — string, stop group identifier
- `stop_pole` — string, stop pole number
- `stop_group_name` — string, human-readable stop name
- `street_id` — string, street identifier
- `latitude` — number, geographic latitude
- `longitude` — number, geographic longitude
- `direction` — string, direction description
- `valid_from` — string, timestamp from which the stop data is valid

### Example request
```bash
curl 'https://warsaw-transport-function-hzg8h7buhbepd0aw.polandcentral-01.azurewebsites.net/api/stops' \
  --header 'x-functions-key: <FUNCTION_KEY>'
```

### Example response
```json
[
  {
    "stop_group": "1001",
    "stop_pole": "01",
    "stop_group_name": "Kijowska",
    "street_id": "2201",
    "latitude": 52.248455,
    "longitude": 21.044827,
    "direction": "al.Zieleniecka",
    "valid_from": "2025-04-01 00:00:00.0"
  }
]
```

### Error responses
- `404` — Stops data not found in cache

---

## 3. Get Lines at Stop

### Purpose
Returns the list of public transport lines available at a given stop. Data is fetched live from the ZTM API.

### Endpoint
- **Method:** `GET`
- **URL:** `/api/stops/{stop_id}/{stop_number}/lines`

### Path parameters
- `stop_id` — stop group identifier (e.g. `"5070"`)
- `stop_number` — stop pole number (e.g. `"03"`)

### Required headers
- `x-functions-key` — Azure Function access key

### Response format
- JSON

### Response structure
- `stop_id` — string, stop group identifier
- `stop_number` — string, stop pole number
- `lines` — array of strings, line designations available at the stop

### Example request
```bash
curl 'https://warsaw-transport-function-hzg8h7buhbepd0aw.polandcentral-01.azurewebsites.net/api/stops/5070/03/lines' \
  --header 'x-functions-key: <FUNCTION_KEY>'
```

### Example response
```json
{
  "stop_id": "5070",
  "stop_number": "03",
  "lines": ["23", "20", "24"]
}
```

### Error responses
- `400` — Missing stop_id or stop_number
- `500` — Error fetching data from ZTM API

---

## 4. Get Departures

### Purpose
Returns departure times for a specific line at a specific stop. Data is fetched live from the ZTM API.

### Endpoint
- **Method:** `GET`
- **URL:** `/api/stops/{stop_id}/{stop_number}/lines/{line}/departures`

### Path parameters
- `stop_id` — stop group identifier
- `stop_number` — stop pole number
- `line` — line number (e.g. `"20"`)

### Required headers
- `x-functions-key` — Azure Function access key

### Response format
- JSON (array)

### Response structure
Each departure entry contains:

- `departure_time` — string, departure time (HH:mm:ss)
- `brigade` — string, brigade / vehicle crew identifier
- `direction` — string, destination description
- `route` — string, route designation
- `symbol_1` — string or null, additional information
- `symbol_2` — string or null, additional information

### Example request
```bash
curl 'https://warsaw-transport-function-hzg8h7buhbepd0aw.polandcentral-01.azurewebsites.net/api/stops/5070/03/lines/20/departures' \
  --header 'x-functions-key: <FUNCTION_KEY>'
```

### Example response
```json
[
  {
    "departure_time": "06:46:00",
    "brigade": "012",
    "direction": "Żerań FSO",
    "route": "TP-FSO",
    "symbol_1": null,
    "symbol_2": null
  }
]
```

### Error responses
- `400` — Missing stop_id, stop_number, or line
- `500` — Error fetching data from ZTM API

---

## Common Notes

- All endpoints return JSON with `Content-Type: application/json`.
- Route and stops data is cached in Cosmos DB and synced daily via timer-triggered functions.
- Lines and departures endpoints fetch live data from the ZTM API on each request.
- The `transport_type` field on routes follows the classification rules documented in [`API_DOCUMENTATION.md`](./API_DOCUMENTATION.md#transport-type-classification).
