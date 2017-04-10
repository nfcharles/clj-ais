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

## Sample Decoding Application

clj-ais is packaged with a sample decoding application*.  It's instrumented with basic logging and metrics.  See examples below.

<sub>*This is a naive implemenation.  Multipart messages received out of order are simply dropped; also, unpaired fragments are dropped.</sub>
### Examples

#### Decode message type 25
```bash
$ export INPUT=source/sample-25.nmea
$ export TYPES=25
$ export OUTPUT=output/sample-decoded-25
$ bin/ais-decode --threads 1 --output-format json $INPUT $TYPES $OUTPUT
JAVA_OPTS=-Xms2048m -Xmx4096m
RELEASE_JAR=target/uberjar/ais-0.9.0-SNAPSHOT-standalone.jar
INPUT=source/sample.gfnmea
MESSAGE_TYPES=25
THREADS=1
OUTPUT_FORMAT=json
OUTPUT_NAME=sample-decoded-25
BUFFER_LEN=50000
INCLUDE TYPES: #{25}
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:67] - count.dropped.total=160149
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:68] - count.invalid.total=4446
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:69] - count.error.total=4446
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-0=5
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-1=74485
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-2=2444
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-3=15587
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-4=5791
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-5=12405
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-6=794
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-7=4
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-8=5273
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-9=299
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-10=5
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-11=69
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-12=1
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-13=2
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-14=10
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-15=201
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-16=2
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-17=1251
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-18=7250
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-19=273
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-20=1237
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-21=13943
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-22=5
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-23=184
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-24=4408
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-27=20
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-40=7
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-41=1
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-49=1
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-58=1
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-61=3
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:155] - count.decoder.thread_0=0
17-04-10 00:41:34 ncharles-XPS-13-9360 INFO [ais.decode:156] - count.decoder.err.thread_0=0
"Elapsed time: 1708.508363 msecs"
```

#### Decode message types 1,2,3,5,18,19,24
```bash
$ export INPUT=source/sample.nmea
$ export TYPES=1,2,3,5,18,19,24
$ export OUTPUT=output/sample-decoded-1-2-3-4-5-18-19-24
$ bin/ais-decode --threads 2 --output-format json $INPUT $TYPES $OUTPUT
JAVA_OPTS=-Xms2048m -Xmx4096m
RELEASE_JAR=target/uberjar/ais-0.9.0-SNAPSHOT-standalone.jar
INPUT=source/sample.gfnmea
MESSAGE_TYPES=1,2,3,5,18,19,24
THREADS=2
OUTPUT_FORMAT=json
OUTPUT_NAME=sample-decoded-1-2-3-5-18-19-24
BUFFER_LEN=50000
INCLUDE TYPES: #{1 24 3 2 19 5 18}
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:67] - count.dropped.total=1434
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:68] - count.invalid.total=12
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:69] - count.error.total=12
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-4=192
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-7=1
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-9=10
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-11=1
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-15=25
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-20=23
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-21=1044
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:71] - count.dropped.type-27=138
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:155] - count.decoder.thread_1=30259
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:164] - writing sample-decoded-1-2-3-5-18-19-24-part-0-0.json
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:156] - count.decoder.err.thread_1=0
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:155] - count.decoder.thread_0=28900
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:156] - count.decoder.err.thread_0=0
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:164] - writing sample-decoded-1-2-3-5-18-19-24-part-1-0.json
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:166] - count.writer.thread_1=28900
17-04-10 01:03:25 ncharles-XPS-13-9360 INFO [ais.decode:166] - count.writer.thread_0=30259
"Elapsed time: 2989.639246 msecs"
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
- Fix logging datetime format
- Implement remaining types

### Testing
- More integration tests


## License

Copyright © 2015 Navil Charles

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
