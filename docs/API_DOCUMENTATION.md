# Warsaw Transport API Documentation

This document summarizes the four Warsaw Transport APIs provided in the user-supplied documentation. The descriptions below are normalized into a consistent structure for easier use in development and integration.

---

## 1. Public Transport Routes

### Purpose
Returns current route data for Warsaw Public Transport vehicles (WTP). The data is provided by the Warsaw Public Transport Authority (`ZTM`).

### Endpoint
- **Method:** `GET`
- **URL:** `https://api.um.warszawa.pl/api/action/public_transport_routes/`

### Update frequency
- Updated **once per day**.

### Required parameters
- `apikey` — API key obtained after creating an account on `api.um.warszawa.pl`.

### Response format
- JSON

### Response structure
The response is a nested JSON object with:
- a **route number** as the top-level key,
- a **route name** as the next-level key,
- a list of **stop sequence numbers** as nested keys.

Each stop entry contains:
- `ulica_id` — street ID where the stop is located
- `nr_zespolu` — stop group number
- `nr_przystanku` — stop number within the group
- `typ` — stop type
- `odleglosc` — distance from the beginning of the route in meters

### Example request
```text
https://api.um.warszawa.pl/api/action/public_transport_routes/?apikey=YOUR_API_KEY
```

### Example response
```json
{
  "1": {
    "TD-3BAN": {
      "1": {
        "ulica_id": "2513",
        "nr_zespolu": "R-03",
        "nr_przystanku": "00",
        "typ": "6",
        "odleglosc": 0
      },
      "2": {
        "ulica_id": "1205",
        "nr_zespolu": "3240",
        "nr_przystanku": "04",
        "typ": "5",
        "odleglosc": 245
      },
      "3": {
        "ulica_id": "1205",
        "nr_zespolu": "3239",
        "nr_przystanku": "04",
        "typ": "1",
        "odleglosc": 833
      },
      "4": {
        "ulica_id": "1205",
        "nr_zespolu": "3238",
        "nr_przystanku": "04",
        "typ": "1",
        "odleglosc": 1228
      },
      "5": {
        "ulica_id": "1205",
        "nr_zespolu": "3237",
        "nr_przystanku": "04",
        "typ": "1",
        "odleglosc": 1643
      },
      "6": {
        "ulica_id": "0303",
        "nr_zespolu": "3286",
        "nr_przystanku": "04",
        "typ": "1",
        "odleglosc": 2016
      },
      "7": {
        "ulica_id": "1903",
        "nr_zespolu": "3118",
        "nr_przystanku": "02",
        "typ": "1",
        "odleglosc": 2383
      },
      "18": {
        "ulica_id": "0202",
        "nr_zespolu": "4108",
        "nr_przystanku": "04",
        "typ": "3",
        "odleglosc": 7112
      }
    }
  }
}
```

---

## 2. Public Transport Stop Locations

### Purpose
Returns location data for public transport stops. The dataset is associated with **Lines available at the stop**.

### Endpoint
- **Method:** `POST`
- **URL:** `https://dane.um.warszawa.pl/api/action/get_ztm_przystanki_komunikacji_miejskiej`

### Required parameters
- `resource_id` — GUID, required resource identifier.
- `Authorization` — Header with value `<ZTM_API_KEY>`, required.

### Response format
- JSON-like structure

### Response structure
The documentation lists the following response fields:

#### Stop / dataset fields
- `ijp` — array, summary air quality index according to the Polish Air Quality Index
  - `name` — string, verbal summary of the air quality index
  - `resource_id` — GUID, required resource identifier
- `data_source` — string, measurement source
- `Name` — string, station name
- `lon` — number, longitude
- `lat` — number, latitude

#### Address object: `adres`
- `city` — string, city
- `street` — string, street
- `zip_code` — string, postal code
- `commune` — string, commune

#### Measurements object: `pomiary`
- `index` — array, air quality index for the measurement
- `param_name` — string, pollutant name
- `param_code` — string, pollutant code
- `value` — string, measurement value
- `time` — string, measurement time
- `unit` — string, unit

### Example request
```bash
curl --location --request POST 'https://dane.um.warszawa.pl/api/action/get_ztm_przystanki_komunikacji_miejskiej' --header 'Authorization: <ZTM_API_KEY>'
```

### Example response
```json
{
  "values": [
    {
      "value": "1001",
      "key": "zespol"
    },
    {
      "value": "01",
      "key": "slupek"
    },
    {
      "value": "Kijowska",
      "key": "nazwa_zespolu"
    },
    {
      "value": "2201",
      "key": "id_ulicy"
    },
    {
      "value": "52.248455",
      "key": "szer_geo"
    },
    {
      "value": "21.044827",
      "key": "dlug_geo"
    },
    {
      "value": "al.Zieleniecka",
      "key": "kierunek"
    },
    {
      "value": "2025-04-01 00:00:00.0",
      "key": "obowiazuje_od"
    }
  ]
}
```

### Notes
The provided documentation mixes air-quality terminology with stop-location terminology. The sample response clearly describes a stop record, so the field list above is normalized from the source material.

---

## 3. Lines Available at the Stop

### Purpose
Returns the list of public transport lines available at a given stop.

### Related datasets
- **Public Transport Stop Locations**
- **Line Departures from Stop**

### Endpoint
- **Method:** `POST`
- **URL:** `http://dane.um.warszawa.pl/api/action/get_ztm_lista_linii_na_przystanku`

### Required parameters
- `busstopId` — stop identifier.
- `busstopNr` — stop pole identifier.
- `Authorization` — Header with value `<ZTM_API_KEY>`, required.

### Response format
- JSON

### Response structure
The response is an array of objects. Each object contains:
- `values` — array of lines available at the stop

Each line entry contains:
- `value` — line number available at the stop
- `key` — fixed value: `"linia"`

### Example request
```bash
curl --location 'http://dane.um.warszawa.pl/api/action/get_ztm_lista_linii_na_przystanku' \
  --header 'Authorization: <ZTM_API_KEY>' \
  --header 'Content-Type: application/json' \
  --data '{"busstopId": "5070", "busstopNr": "03" }'
```

### Example response
```json
[
  {
    "values": [
      {
        "value": "23",
        "key": "linia"
      }
    ]
  },
  {
    "values": [
      {
        "value": "20",
        "key": "linia"
      }
    ]
  },
  {
    "values": [
      {
        "value": "24",
        "key": "linia"
      }
    ]
  }
]
```

---

## 4. Line Departures from the Stop

### Purpose
Returns departures for a given line at a specific stop.

### Related datasets
- **Location of public transport stops**
- **Lines available at the stop**

### Endpoint
- **Method:** `POST`
- **URL:** `https://dane.um.warszawa.pl/api/action/get_ztm_odjazdy_linii_z_przystanku`

### Required parameters
- `busstopId` — stop identifier.
- `busstopNr` — stop pole identifier.
- `line` — line number.
- `Authorization` — Header with value `<ZTM_API_KEY>`, required.

### Response format
- JSON

### Response structure
Each departure entry contains:
- `czas` — departure time
- `brygada` — identifier of the brigade/vehicle crew serving the trip
- `kierunek` — destination/direction of the trip
- `trasa` — route being operated
- `symbol_1` — additional info
- `symbol_2` — additional info

### Example request
```bash
curl --location 'https://dane.um.warszawa.pl/api/action/get_ztm_odjazdy_linii_z_przystanku' --header 'Authorization: <ZTM_API_KEY>' --header 'Content-Type: application/json' --data '{"busstopId": "5070", "busstopNr": "03", "line": "20"}'
```

### Example response
```json
[
  [
    {
      "value": null,
      "key": "symbol_2"
    },
    {
      "value": null,
      "key": "symbol_1"
    },
    {
      "value": "012",
      "key": "brygada"
    },
    {
      "value": "Żerań FSO",
      "key": "kierunek"
    },
    {
      "value": "TP-FSO",
      "key": "trasa"
    },
    {
      "value": "06:46:00",
      "key": "czas"
    }
  ]
]
```

---

## Common Notes

- All APIs use Warsaw transport data from the `api.um.warszawa.pl` or `dane.um.warszawa.pl` domains.
- Some source documentation is inconsistent in wording and field naming; this file preserves the intent and normalizes the summaries into a consistent format.
- Example requests use the exact shapes shown in the source documentation.

