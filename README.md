# clj-ais

clj-ais is a library for decoding ais messages.  It was designed with reference to the following document http://catb.org/gpsd/AIVDM.html#IMO236 and tested against the following online decoder -- which support a subset of message types -- http://rl.se/aivdm.  Also, integration tests were performed against http://fossies.org/linux/gpsd/test/sample.aivdm, which contains an exhastive list of ais messages and their decoded values.


#### Supported Types
| Type | Description                    |
| ---- | ------------------------------ |
| 1    | Position Report Class A        |
| 2    | Position Report Class A        |
| 3    | Position Report Class A        |
| 4    | Base Station Report            |
| 5    | Static and Voyage Related Data |

While the above messages are the only types supported today, clj-ais has the necessary interfaces for extending type support.  See _Extending_ for more information about extending the library.

## Installation

Download from http://example.com/FIXME.

## Usage

The library contains an entrypoint for a sample implementation decoding app

    $ java -jar ais-0.1.0-SNAPSHOT-standalone.jar [MSG-TYPE-BIT-MASK] [N-THREADS]

## Options

MSG-TYPE-BIT-MASK: bit mask representing message types to decode.

#### Examples
| mask | messages |
|------|----------|
|  3   |   1,2    |
|  5   |   1,3    |
| 16   |    5     |
| 23   | 1,2,3,5  |

| type | decimal value |
|------|---------------|
| 1    |      2^0      |
| 2    |      2^1      |
| 3    |      2^2      |
| ..   |      ..       |
| n    |    2^(n-1)    |

N-THREADS:         number of decoding threads

## Examples

###  Decoding messages
#### type 1, 2, 3 

    $ cat ais-messages-simple | java -jar ais-0.1.0-SNAPSHOT-standalone.jar 7 1

```bash
thread-0
Dropping [type=12] \!AIVDM,1,1,,A,<02:oP0kKcv0@<51C5PB5@?BDPD?P:?2?EB7PDB16693P381>>5<PikP,0*37
writing 20 messages.
writing /tmp/foo.json
"Elapsed time: 37.288095 msecs"

```
  
#### type 5
    $  cat ais-messages-type-5 | java -jar ais-0.1.0-SNAPSHOT-standalone.jar 16 3 2>dropped.log

```bash
thread-0
thread-1
thread-2
writing 213 messages.
writing /tmp/foo.json
"Elapsed time: 554.953214 msecs"
```

#### Single sentence decoding - type 1
```bash
$ lein run -m ais.core "\!AIVDM,1,1,,A,15RTgt0PAso;90TKcjM8h6g208CQ,0*4A" | python -m json.tool
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
    "status": "Under way using engine", 
    "turn": -720
}
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
(def base-mapping (list
  {:len  2 :desc "Repeat Indicator"         :tag "repeat"   :fn ais-types/u}
  {:len 30 :desc "MMSI"                     :tag "mmsi"     :fn ais-types/u}
  {:len  4 :desc "Navigation Status"        :tag "status"   :fn (partial ais-types/e ais-vocab/navigation-status)}
  {:len  8 :desc "Rate of Turn (ROT)"       :tag "turn"     :fn (partial ais-types/I (/ 1.0 4.733) 3 #(* %1 %1))}
  {:len 10 :desc "Speed Over Ground (SOG)"  :tag "speed"    :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len  1 :desc "Position Accuracy"        :tag "accuracy" :fn ais-types/b}
  {:len 28 :desc "Longtitude"               :tag "lon"      :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len 27 :desc "Latitude"                 :tag "lat"      :fn (partial ais-types/I (/ 1.0 600000) 4)}
  {:len 12 :desc "Course Over Ground (COG)" :tag "course"   :fn (partial ais-types/U (/ 1.0 10) 1)}
  {:len  9 :desc "True Heading (HDG)"       :tag "heading"  :fn ais-types/u}
  {:len  6 :desc "Time Stamp"               :tag "second"   :fn ais-types/u}
  {:len  2 :desc "Maneuver Indicator"       :tag "maneuver" :fn (partial ais-types/e ais-vocab/maneuver-indicator)}
  {:len  3 :desc "Spare"                    :tag "spare"    :fn ais-types/x}
  {:len  1 :desc "RAIM flag"                :tag "raim"     :fn ais-types/b}
  {:len 19 :desc "Radio status"             :tag "radio"    :fn ais-types/u}
))
```

The following represents an unpacked sentence payload for a type 1 message: (```177KQJ5000G?tO\`K>RA1wUbN0TKH```):
```bash
000011100001010100111001100000000000000010001101000111110010100001101000010000011101001011100000000000000001010001001000000100111001001100100000000101011100101100000000000000000000000000000000000000000000000000000000000000000001001111000001111010000000001011001011000110101111001111000000001111100010010101000001010000011000001000000001110100100001010001
```
The ```:len``` field in the field specification map is the number of bits representing the particular field.

Adding support for a new message type requires creating a new type specification mapping with properly implemented type handler deserializers ```:fn```.  See http://catb.org/gpsd/AIVDM.html for more information.

## TODO
### lib
- Decoder module writes files to hardcoded /tmp/foo.json  -- make runtime parameter
- Parse timestamp field from message tag block
- .json is standard output format.  Support .csv output also.
- Implement more message types: 18,24,19,21,20

### testing
- More unit tests
- More integration tests



## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
