# clj-ais

clj-ais is an AIS (Automatic Identification System) message decoding library adhering to the following de facto standard:
  - http://catb.org/gpsd/AIVDM.html

Decoded messages were validated against the following online decoder
  - http://www.aggsoft.com/ais-decoder.htm

### Supported Types
| Type  | Description                           | Implemented | *Tested |
|-------| --------------------------------------|-------------|---------|
|   1   | Position Report Class A               |      x      |    x    |
|   2   | Position Report Class A               |      x      |    x    |
|   3   | Position Report Class A               |      x      |    x    |
|   4   | Base Station Report                   |      x      |    x    |
|   5   | Static and Voyage Related Data        |      x      |    x    |
|   6** | Binary Addressed Message              |      x      |    x    |
|   7   | Binary Acknowledge                    |      x      |         |
|   8** | Binary Broadcast Message              |      x      |    x    |
|   9   | Standard SAR Aircraft Position Report |      x      |         |
|  10   | UTC/Date Inquiry                      |      x      |         |
|  11   | UTC/Date Response                     |      x      |    x    |
|  12   | Addressed Safety-Related Message      |      x      |         |
|  13   | Safety-Related Acknowledgement        |      x      |         |
|  14   | Safety-Related Broadcast Message      |      x      |         |
|  15   | Interrogation                         |      x      |    x    |
|  16   | Assignment Mode Command               |      x      |         |
|  17   | DGNSS Binary Broadcast Message        |      x      |    x    |
|  18   | Standard Class B CS Position Report   |      x      |    x    |
|  19   | Extended Class B CS Position Report   |      x      |    x    |
|  20   | Data Link Management Message          |      x      |         |
|  21   | Aid-to-Navigation Report              |      x      |         |
|  22   | Channel Management                    |      x      |    x    |
|  23   | Group Assignment Command              |      x      |    x    |
|  24   | Static Data Report                    |      x      |    x    |
|  25   | Single Slot Binary Message            |      x      |    x    |
|  26   | Multi Slot Bin Message w/ Comm State  |      x      |    x    |
|  27   | Position Report For Long-Range Apps   |      x      |    x    |

<sub>** Subtypes that are not implemented use generic type decoding.</sub>

#### Type 6 subtypes
| DAC  | FID | Description                  | Implemented | *Tested |
|------|-----|------------------------------|-------------|---------|
|   1  | 12  | Dangerous cargo indication   |      x      |         |
|   1  | 14  | Tidal window                 |      x      |         |
|   1  | 16  | Number of persons on board   |      x      |         |
|   1  | 18  | Clearance time to enter port |      x      |         |
|   1  | 20  | Berthing data (addressed)    |             |         |
|   1  | 23  | Area notice (addressed)      |      x      |         |
|   1  | 25  | Dangerous cargo indication   |             |         |
|   1  | 28  | Route info addressed         |             |         |
|   1  | 30  | Text description addressed   |             |         |
|   1  | 32  | Tidal window                 |             |         |
| 200  | 21  | ETA at lock/bridge/terminal  |      x      |         |
| 200  | 22  | RTA at lock/bridge/termina   |      x      |         |
| 200  | 55  | Number of persons on board   |      x      |         |
| 235  | 10  | AtoN monitoring data (UK)    |      x      |         |
| 250  | 10  | AtoN monitoring data (ROI)   |      x      |         |

#### Type 8 subtypes
| DAC     | FID | Description                         | Implemented | *Tested |
|---------|-----|-------------------------------------|-------------|---------|
|       1 |  11 | Meteorological/Hydrological Data    |             |         |
|       1 |  13 | Fairway closed                      |             |         |
|       1 |  15 | Extended ship and voyage            |             |         |
|       1 |  17 | VTS-Generated/Synthetic targets     |             |         |
|       1 |  19 | Marine traffic signals              |             |         |
|       1 |  21 | Weather observation from ship       |             |         |
|       1 |  22 | Area notice (broadcast)             |             |         |
|       1 |  24 | Extended ship and voyage            |             |         |
|       1 |  26 | Environmental                       |             |         |
|       1 |  27 | Route info broadcast                |             |         |
|       1 |  29 | Text description broadcast          |             |         |
|       1 |  31 | Meteorological and hydrological     |      x      |    x    |
|     200 |  10 | Ship static and voyage related data |      x      |    x    |
|     200 |  23 | EMMA warning report                 |             |         |
|     200 |  24 | Water levels                        |      x      |    x    |
|     200 |  40 | Signal status                       |             |         |

<sub>*Tested: This refers to live testing against actual AIS sentences.</sub>

## Installation

Fork repo and build.

```bash
$ cd /path/to/clj-ais
$ lein ubjarbar
```

## Examples

### Single sentence decoding

#### Type 1
```bash
>  java -jar target/uberjar/ais-0.9.3-SNAPSHOT-standalone.jar "\c:1521252608,t:1521252607*00\!AIVDM,1,1,,B,159tImgP01IEFP0:S0kRTgv>00Ru,0*68" | python -m json.tool 
```

#### Output
```json
{
    "accuracy": false,
    "course": 65.8,
    "heading": 511,
    "lat": 18.436029802,
    "line": null,
    "lon": -93.221413888,
    "maneuver": "Not available (default)",
    "mmsi": 345971158,
    "radio": 2237,
    "raim": false,
    "repeat": 0,
    "second": 7,
    "spare": "000",
    "speed": 0.1,
    "station": null,
    "status": "Not defined (default)",
    "timestamp": "20180317021008",
    "turn": -731,
    "type": 1
}
```

#### Type 24
```bash
> java -jar target/uberjar/ais-0.9.3-SNAPSHOT-standalone.jar "\c:1521252608*00\!AIVDM,1,1,,A,H7Og65QA8UHT4j11E9=DU@00000,2*6F" | python -m json.tool 
```

#### Output
```json
{
    "line": null,
    "mmsi": 503039510,
    "partno": 0,
    "repeat": 0,
    "shipname": "TRIVIAL PURSUIT",
    "spare": "0000",
    "station": null,
    "timestamp": "20180317021008",
    "type": 24
}
```



### Multi setence decoding

#### Type 5
```bash
> java -jar target/uberjar/ais-0.9.3-SNAPSHOT-standalone.jar "\c:1521252608*00\!AIVDM,2,1,2,A,53Wks?T00000PM8H001a<tl8u8000000000000160HE45tfA03jkk@DSSh00,0*04" "\c:1521252608*00\!AIVDM,2,2,2,A,00000000000,2*26" | python -m json.tool 
```

#### Output
```json
{
    "ais_version": 1,
    "callsign": "HGRF",
    "day": 28,
    "destination": "KOMARNO",
    "draught": 1.5,
    "dte": false,
    "epfd": null,
    "hour": 17,
    "imo": 0,
    "line": null,
    "minute": 0,
    "mmsi": 243071806,
    "month": 2,
    "repeat": 0,
    "shipname": "ZSOMBOR",
    "shiptype": "Cargo, all ships of this type",
    "spare": "0",
    "station": null,
    "timestamp": "20180317021008",
    "to_bow": 3,
    "to_port": 4,
    "to_starboard": 5,
    "to_stern": 21,
    "type": 5
}
```


## Testing

Run all tests.

    $ lein test

Run module tests

    $ lein test ais.util-test

Run integration tests

    $ lein test ais.integration.type_1-test

## TODO

### Implementation
- Implement remaining sub-types

### Testing
- More integration tests


## License

Copyright © 2015 Navil Charles

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
