# clj-ais

clj-ais is an AIS (Automatic Identification System) message decoding library adhering to the following de facto standard:
  - http://catb.org/gpsd/AIVDM.html

Decoded messages were validated against the following online decoder
  - http://www.aggsoft.com/ais-decoder.htm

### Supported Types
| Type | Description                           | Implemented | *Tested |
| ---- | --------------------------------------|-------------|---------|
|  1   | Position Report Class A               |      x      |    x    |
|  2   | Position Report Class A               |      x      |    x    |
|  3   | Position Report Class A               |      x      |    x    |
|  4   | Base Station Report                   |      x      |    x    |
|  5   | Static and Voyage Related Data        |      x      |    x    |
|  **6 | Binary Addressed Message              |      x      |    x    |
|  7   | Binary Acknowledge                    |      x      |         |
|  8   | Binary Broadcast Message              |             |         |
|  9   | Standard SAR Aircraft Position Report |      x      |         |
| 10   | UTC/Date Inquiry                      |      x      |         |
| 11   | UTC/Date Response                     |             |         |
| 12   | Addressed Safety-Related Message      |      x      |         |
| 13   | Safety-Related Acknowledgement        |             |         |
| 14   | Safety-Related Broadcast Message      |      x      |         |
| 15   | Interrogation                         |             |         |
| 16   | Assingment Mode Command               |             |         |
| 17   | DGNSS Binary Broadcast Message        |             |         |
| 18   | Standard Class B CS Position Report   |      x      |    x    |
| 19   | Extended Class B CS Position Report   |      x      |    x    |
| 20   | Data Link Management Message          |      x      |         |
| 21   | Aid-to-Navigation Report              |      x      |         |
| 22   | Channel Management                    |             |         |
| 23   | Group Assignment Command              |             |         |
| 24   | Static Data Report                    |      x      |    x    |
| 25   | Single Slot Binary Message            |             |         |
| 26   | Multi Slot Bin Message w/ Comm State  |             |         |
| 27   | Position Report For Long-Range Apps   |             |         |
<sub>** generic type implemented.  binary data is not decoded</sub>

#### Type 6 subtypes
| DAC  | FID | Description                  | Implemented | *Tested |
|------|-----|------------------------------|-------------|---------|
|   1  | 12  | Dangerous cargo indication   |             |         |
|   1  | 14  | Tidal window                 |             |         |
|   1  | 16  | Number of persons on board   |             |         |
|   1  | 18  | Clearance time to enter port |             |         |
|   1  | 20  | Berthing data (addressed)    |             |         |
|   1  | 23  | Area notice (addressed)      |             |         |
|   1  | 25  | Dangerous cargo indication   |             |         |
|   1  | 28  | Route info addressed         |             |         |
|   1  | 30  | Text description addressed   |             |         |
|   1  | 32  | Tidal window                 |             |         |
| 200  | 21  | ETA at lock/bridge/terminal  |             |         |
| 200  | 22  | RTA at lock/bridge/termina   |             |         |
| 200  | 55  | Number of persons on board   |             |         |
| 235  | 10  | AtoN monitoring data (UK)    |             |         |
| 250  | 10  | AtoN monitoring data (ROI)   |             |         |

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
|       1 |  31 | Meteorological and hydrological     |             |         |
|     200 |  10 | Ship static and voyage related data |             |         |
|     200 |  23 | EMMA warning report                 |             |         |
|     200 |  24 | Water levels                        |             |         |
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

#### Type 1 - json output
```bash
$ lein run -m ais.core json "\c:1448312100,t:1448312099*00\!AIVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0*4A" | python -m json.tool
```

#### Output
```json
{
    "accuracy": true, 
    "course": 224.0, 
    "heading": 215, 
    "lat": 48.38163333333333, 
    "lon": -123.39538333333333, 
    "maneuver": "Not available (default)", 
    "mmsi": 371798000, 
    "radio": 34017, 
    "raim": false, 
    "repeat": 0, 
    "second": 33, 
    "spare": "000", 
    "speed": 12.3, 
    "station": null,
    "status": "Under way using engine", 
    "timestamp": "20151123T155500Z",
    "turn": -720,
    "type": 3
}
```

#### Type 1 - csv output
```bash
$ lein run -m ais.core csv "\c:1448312100,t:1448312099*00\!AIVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0*4A" | python -m json.tool
```

#### Output
```json
[
    1
    "20151123T155500Z", 
    null,
    null,
    0, 
    371798000, 
    "Under way using engine", 
    -720, 
    12.3, 
    true, 
    -123.39538333333333, 
    48.38163333333333, 
    224.0, 
    215, 
    33, 
    "Not available (default)", 
    "000", 
    false, 
    34017
]
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
- Implement remaining types

### Testing
- More unit tests
- More integration tests


## License

Copyright © 2015 Navil Charles

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
