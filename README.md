# clj-ais

clj-ais is a library for decoding AIS (Automatic Identification System) messages - reference (http://catb.org/gpsd/AIVDM.html). The following were used as testing sources
 - http://rl.se/aivdm. 
 - http://fossies.org/linux/gpsd/test/sample.aivdm


#### Supported Types
| Type | Description                         |
| ---- | ------------------------------------|
|  1   | Position Report Class A             |
|  2   | Position Report Class A             |
|  3   | Position Report Class A             |
|  4   | Base Station Report                 |
|  5   | Static and Voyage Related Data      |
| 18   | Standard Class B CS Position Report |
| 19   | Extended Class B CS Position Report |
| 21   | Aid-to-Navigation Report            |
| 20   | Data Link Management Message        |
| 24   | Static Data Report                  |

While the above messages are the only types supported today, clj-ais has the necessary interfaces for extending type support.  See _Extending_ for more information about extending the library.

## Installation

clj-ais is not available in public clojure repos yet.  Fork repo and build.

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

### Bugs
There are no known bugs -- which is not to say there are NO bugs.  Current unit test suite passes but is by no means exhaustive.

## Extending

Sentences are decoded in accordance with type specification mappings.  The data structure is a list of maps where each map represents a field of an uncompressed message -- bit field representation -- and the requisite components necessary to decode it.

```clojure
(def mapping-5 (list
  {:len   6 :desc "Message Type"           :tag "type"         :fn (partial const 5)}
  {:len   2 :desc "Repeat Indicator"       :tag "repeat"       :fn ais-types/u}
  {:len  30 :desc "MMSI"                   :tag "mmsi"         :fn ais-types/u}
  {:len   2 :desc "AIS Version"            :tag "ais_version"  :fn ais-types/u}
  {:len  30 :desc "IMO Number"             :tag "imo"          :fn ais-types/u}
  {:len  42 :desc "Call Sign"              :tag "callsign"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 7)}
  {:len 120 :desc "Vessel Name"            :tag "shipname"     :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   8 :desc "Ship Type"              :tag "shiptype"     :fn (partial ais-types/e ais-vocab/ship-type)}
  {:len   9 :desc "Dimension to Bow"       :tag "to_bow"       :fn ais-types/u}
  {:len   9 :desc "Dimension to Stern"     :tag "to_stern"     :fn ais-types/u}
  {:len   6 :desc "Dimension to Port"      :tag "to_port"      :fn ais-types/u}
  {:len   6 :desc "Dimension to Starboard" :tag "to_starboard" :fn ais-types/u}
  {:len   4 :desc "Position Fix Type"      :tag "epfd"         :fn (partial ais-types/e ais-vocab/position-fix-type)}
  {:len   4 :desc "ETA month (UTC)"        :tag "month"        :fn ais-types/u}
  {:len   5 :desc "ETA day (UTC)"          :tag "day"          :fn ais-types/u}
  {:len   5 :desc "ETA hour (UTC)"         :tag "hour"         :fn ais-types/u}
  {:len   6 :desc "ETA minute (UTC)"       :tag "minute"       :fn ais-types/u}
  {:len   8 :desc "Draught"                :tag "draught"      :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len 120 :desc "Destination"            :tag "destination"  :fn (partial ais-types/t ais-vocab/sixbit-ascii 20)}
  {:len   1 :desc "DTE"                    :tag "dte"          :fn ais-types/b}
  {:len   1 :desc "Spare"                  :tag "spare"        :fn ais-types/x}
))

```

The following represents an unpacked sentence payload for a type 1 message: (```177KQJ5000G?tO\`K>RA1wUbN0TKH```):
```bash
000011100001010100111001100000000000000010001101000111110010100001101000010000011101001011100000000000000001010001001000000100111001001100100000000101011100101100000000000000000000000000000000000000000000000000000000000000000001001111000001111010000000001011001011000110101111001111000000001111100010010101000001010000011000001000000001110100100001010001
```
The ```:len``` field in the field specification map is the number of bits representing the particular field.

Adding support for a new message type requires creating a new type specification mapping with properly implemented field deserializers ```:fn```.  See http://catb.org/gpsd/AIVDM.html#_ais_payload_interpretation for more information.  Finally, you must extend the ```ais-mapping/parsing-rules``` multimethod in order to register the new mapping.

```clojure
;; Type 25 decoding specification
(defmethod parsing-rules 25 [bits] mapping-25)
```

## TODO

### Testing
- More unit tests
- More integration tests


## License

Copyright © 2015 Navil Charles

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
