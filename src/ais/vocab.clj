(ns ais.vocab
  (:require [clojure.string :as string])
  (:gen-class))

(def navigation-status {
  0 "Under way using engine"
  1 "At Anchor"
  2 "Not under command"
  3 "Restricted maneuverability"
  4 "Contrained by her draught"
  5 "Moored"
  6 "Aground"
  7 "Engaged in fishing"
  8 "Under way sailing"
  9 "Reserved for future amendment of Navigational Status for HSC"
 10 "Reserved for future amendment of Navigational Status for WIG"
 11 "Reserved for future use"
 12 "Reserved for future use"
 13 "Reserved for future use"
 14 "AIS-SART is active"
 15 "Not defined (default)"
})


(def maneuver-indicator {
 0 "Not available (default)"
 1 "No special maneuver"
 2 "Special maneuver (such as regional passing arrangement)"
})

(def position-fix-type {
 0 "Undefined (default)"
 1 "GPS"
 2 "GLONASS"
 3 "Combined GPS/GLONASS"
 4 "Loran-C"
 5 "Chayka"
 6 "Integrated navigation system"
 7 "Surveyed"
 8 "Galileo"
})


(def ship-type {
  0 "Not Available"
  1 "Reserved for future use"
  2 "Reserved for future use"
  3 "Reserved for future use"
  4 "Reserved for future use"
  5 "Reserved for future use"
  6 "Reserved for future use"
  7 "Reserved for future use"
  8 "Reserved for future use"
  9 "Reserved for future use"
 10 "Reserved for future use"
 11 "Reserved for future use"
 12 "Reserved for future use"
 13 "Reserved for future use"
 14 "Reserved for future use"
 15 "Reserved for future use"
 16 "Reserved for future use"
 17 "Reserved for future use"
 18 "Reserved for future use"
 19 "Reserved for future use"
 20 "Wing in ground (WIG), all ships of this type "
 21 "Wing in ground (WIG), Hazardous category A"
 22 "Wing in ground (WIG), Hazardous category B"
 23 "Wing in ground (WIG), Hazardous category C"
 24 "Wing in ground (WIG), Hazardous category D"
 25 "Wing in ground (WIG), Reserved for future use"
 26 "Wing in ground (WIG), Reserved for future use"
 27 "Wing in ground (WIG), Reserved for future use"
 28 "Wing in ground (WIG), Reserved for future use"
 29 "Wing in ground (WIG), Reserved for future use"
 30 "Fishing"
 31 "Towing"
 32 "Towing: length exceeds 200m or breadth exceeds 25m"
 33 "Dredging or underwater ops"
 34 "Diving ops"
 35 "Military ops"
 36 "Sailing"
 37 "Pleasure Craft"
 38 "Reserved"
 39 "Reserved"
 40 "High speed craft (HSC), all ships of this type"
 41 "High speed craft (HSC), Hazardous catergory A"
 42 "High speed craft (HSC), Hazardous catergory B"
 43 "High speed craft (HSC), Hazardous catergory C"
 44 "High speed craft (HSC), Hazardous catergory D"
 45 "High speed craft (HSC), Reserved for future use"
 46 "High speed craft (HSC), Reserved for future use"
 47 "High speed craft (HSC), Reserved for future use"
 48 "High speed craft (HSC), Reserved for future use"
 49 "High speed craft (HSC), No additional information"
 50 "Pilot Vessel"
 51 "Search and Rescue vessel"
 52 "Tug"
 53 "Port Tender"
 54 "Anti-pollution equipment"
 55 "Law Enforcement"
 56 "Spare - Local Vessel"
 57 "Spare - Local Vessel"
 58 "Medical Transport"
 59 "Noncombatant ship according to RR Resolution No. 18"
 60 "Passenger, all ships of this type"
 61 "Passenger, Hazardous category A"
 62 "Passenger, Hazardous category B"
 63 "Passenger, Hazardous category C"
 64 "Passenger, Hazardous category D"
 65 "Passenger, Reserved for future use"
 66 "Passenger, Reserved for future use"
 67 "Passenger, Reserved for future use"
 68 "Passenger, Reserved for future use"
 69 "Passenger, No additional information"
 70 "Cargo, all ships of this type"
 71 "Cargo, Hazardous A"
 72 "Cargo, Hazardous B"
 73 "Cargo, Hazardous C"
 74 "Cargo, Hazardous D"
 75 "Cargo, Reserved for future use"
 76 "Cargo, Reserved for future use"
 77 "Cargo, Reserved for future use"
 78 "Cargo, Reserved for future use"
 79 "Cargo, No additional information"
 80 "Tanker, all ships of this type"
 81 "Tanker, Hazardous category A"
 82 "Tanker, Hazardous category B"
 83 "Tanker, Hazardous category C"
 84 "Tanker, Hazardous category D"
 85 "Tanker, Reserved for future use"
 86 "Tanker, Reserved for future use"
 87 "Tanker, Reserved for future use"
 88 "Tanker, Reserved for future use"
 89 "Tanker, No additional information"
 90 "Other Type, all ships of this type"
 91 "Other Type, Hazardous category A"
 92 "Other Type, Hazardous category B"
 93 "Other Type, Hazardous category C"
 94 "Other Type, Hazardous category D"
 95 "Other Type, Reserved for future use"
 96 "Other Type, Reserved for future use"
 97 "Other Type, Reserved for future use"
 98 "Other Type, Reserved for future use"
 99 "Other Type, No additional information"
})

(def sixbit-ascii {
  0 "@"  1 "A"  2 "B"  3 "C"  4 "D"
  5 "E"  6 "F"  7 "G"  8 "H"  9 "I"
 10 "J" 11 "K" 12 "L" 13 "M" 14 "N"
 15 "O" 16 "P" 17 "Q" 18 "R" 19 "S"
 20 "T" 21 "U" 22 "V" 23 "W" 24 "X"
 25 "Y" 26 "Z" 27 "[" 28 "\\" 29 "]"
 30 "^" 31 "_" 32 " " 33 "!" 34 "\""
 35 "#" 36 "$" 37 "%" 38 "&" 39 "'"
 40 "(" 41 ")" 42 "*" 43 "+" 44 ","
 45 "-" 46 "." 47 "/" 48 "0" 49 "1" 
 50 "2" 51 "3" 52 "4" 53 "5"  54 "6"
 55 "7" 56 "8" 57 "9" 58 ":" 59 ";"
 60 "<" 61 "=" 62 ">" 63 "?"
})

(def char-str->dec {
 "0"  0 "1"  1 "2"  2 "3"  3 "4"  4
 "5"  5 "6"  6 "7"  7 "8"  8 "9"  9
 ":" 10 ";" 11 "<" 12 "=" 13 ">" 14
 "?" 15 "@" 16 "A" 17 "B" 18 "C" 19
 "D" 20 "E" 21 "F" 22 "G" 23 "H" 24
 "I" 25 "J" 26 "K" 27 "L" 28 "M" 29
 "N" 30 "O" 31 "P" 32 "Q" 33 "R" 34
 "S" 35 "T" 36 "U" 37 "V" 38 "W" 39
 "`" 40 "a" 41 "b" 42 "c" 43 "d" 44
 "e" 45 "f" 46 "g" 47 "h" 48 "i" 49
 "j" 50 "k" 51 "l" 52 "m" 53 "n" 54
 "o" 55 "p" 56 "q" 57 "r" 58 "s" 59
 "t" 60 "u" 61 "v" 62 "w" 63
})

(def char-str->bits {
 "0" "000000" "1" "000001" "2" "000010"
 "3" "000011" "4" "000100" "5" "000101"
 "6" "000110" "7" "000111" "8" "001000"
 "9" "001001" ":" "001010" ";" "001011"
 "<" "001100" "=" "001101" ">" "001110"
 "?" "001111" "@" "010000" "A" "010001"
 "B" "010010" "C" "010011" "D" "010100"
 "E" "010101" "F" "010110" "G" "010111"
 "H" "011000" "I" "011001" "J" "011010"
 "K" "011011" "L" "011100" "M" "011101"
 "N" "011110" "O" "011111" "P" "100000"
 "Q" "100001" "R" "100010" "S" "100011"
 "T" "100100" "U" "100101" "V" "100110"
 "W" "100111" "`" "101000" "a" "101001"
 "b" "101010" "c" "101011" "d" "101100"
 "e" "101101" "f" "101110" "g" "101111"
 "h" "110000" "i" "110001" "j" "110010"
 "k" "110011" "l" "110100" "m" "110101"
 "n" "110110" "o" "110111" "p" "111000"
 "q" "111001" "r" "111010" "s" "111011"
 "t" "111100" "u" "111101" "v" "111110"
 "w" "111111"
})

; -----

(def char->dec {
 \0  0 \1  1 \2  2 \3  3 \4  4
 \5  5 \6  6 \7  7 \8  8 \9  9
 \: 10 \; 11 \< 12 \= 13 \> 14
 \? 15 \@ 16 \A 17 \B 18 \C 19
 \D 20 \E 21 \F 22 \G 23 \H 24
 \I 25 \J 26 \K 27 \L 28 \M 29
 \N 30 \O 31 \P 32 \Q 33 \R 34
 \S 35 \T 36 \U 37 \V 38 \W 39
 \` 40 \a 41 \b 42 \c 43 \d 44
 \e 45 \f 46 \g 47 \h 48 \i 49
 \j 50 \k 51 \l 52 \m 53 \n 54
 \o 55 \p 56 \q 57 \r 58 \s 59
 \t 60 \u 61 \v 62 \w 63
})

(def char->bits {
 \0 "000000" \1 "000001" \2 "000010"
 \3 "000011" \4 "000100" \5 "000101"
 \6 "000110" \7 "000111" \8 "001000"
 \9 "001001" \: "001010" \; "001011"
 \< "001100" \= "001101" \> "001110"
 \? "001111" \@ "010000" \A "010001"
 \B "010010" \C "010011" \D "010100"
 \E "010101" \F "010110" \G "010111"
 \H "011000" \I "011001" \J "011010"
 \K "011011" \L "011100" \M "011101"
 \N "011110" \O "011111" \P "100000"
 \Q "100001" \R "100010" \S "100011"
 \T "100100" \U "100101" \V "100110"
 \W "100111" \` "101000" \a "101001"
 \b "101010" \c "101011" \d "101100"
 \e "101101" \f "101110" \g "101111"
 \h "110000" \i "110001" \j "110010"
 \k "110011" \l "110100" \m "110101"
 \n "110110" \o "110111" \p "111000"
 \q "111001" \r "111010" \s "111011"
 \t "111100" \u "111101" \v "111110"
 \w "111111"
})

(def bitmask {
  1  0x1
  2  0x3
  3  0x7
  4  0xF
  5  0x1F
  6  0x3F
  7  0x7F
  8  0xFF
  9  0x1FF
 10  0x3FF
 11  0x7FF
 12  0xFFF
 13  0x1FFF
 14  0x3FFF
 15  0x7FFF
 16  0xFFFF
 17  0x1FFFF
 18  0x3FFFF
 19  0x7FFFF
 20  0xFFFFF
 21  0x1FFFFF
 22  0x3FFFFF
 23  0x7FFFFF
 24  0xFFFFFF
 25  0x1FFFFFF
 26  0x3FFFFFF
 27  0x7FFFFFF
 28  0xFFFFFFF
 29  0x1FFFFFFF
 30  0x3FFFFFFF
 31  0x7FFFFFFF
 32  0xFFFFFFFF
})

(def nav-aid-type {
  0 "Default, Type of Aid to Navigation not specified"
  1 "Reference point"
  2 "RACON (radar transponder marking a navigation hazard)"
  3 "Fixed structure off shore, such as oil platforms, wind farms, rigs"
  4 "Spare, Reserved fro future use"
  5 "Light, without sectors"
  6 "Light, with sectors"
  7 "Leading Light Front"
  8 "Leading Light Rear"
  9 "Beacon, Cardinal N"
 10 "Beacon, Cardinal E"
 11 "Beacon, Cardinal S"
 12 "Beacon, Cardinal W"
 13 "Beacon, Port hand"
 14 "Beacon, Starboard hand"
 15 "Beacon, Preferred Channel port hand"
 16 "Beacon, Preferred Channel starboard hand"
 17 "Beacon, Isolated danger"
 18 "Beacon, Safe water"
 19 "Beacon, Special mark"
 20 "Cardinal Mark N"
 21 "Cardinal Mark E"
 22 "Cardinal Mark S"
 23 "Cardinal Mark W"
 24 "Port hand Mark"
 25 "Starboard hand Mark"
 26 "Preferred Channel Port hand"
 27 "Preferred Channel Starboard hand"
 28 "Isolated danger"
 29 "Safe Water"
 30 "Special Mark"
 31 "Light Vessel / LANBY / Rigs"
})

(def cargo-unit-code {
 0 "Not available (default)"
 1 "kg"
 2 "metric tons"
 3 "metric kilotons"
})

(def area-notice-description {
   0 "Caution Area: Marine mammals habitat"
   1 "Caution Area: Marine mammals in area - reduce speed"
   2 "Caution Area: Marine mammals in area - stay clear"
   3 "Caution Area: Marine mammals in area - report sightings"
   4 "Caution Area: Protected habitat - reduce speed"
   5 "Caution Area: Protected habitat - stay clear"
   6 "Caution Area: Protected habitat - no fishing or anchoring"
   7 "Caution Area: Derelicts (drifting objects)"
   8 "Caution Area: Traffic congestion"
   9 "Caution Area: Marine event"
  10 "Caution Area: Divers down"
  11 "Caution Area: Swim area"
  12 "Caution Area: Dredge operations"
  13 "Caution Area: Survey operations"
  14 "Caution Area: Underwater operation"
  15 "Caution Area: Seaplane operations"
  16 "Caution Area: Fishery – nets in water"
  17 "Caution Area: Cluster of fishing vessels"
  18 "Caution Area: Fairway closed"
  19 "Caution Area: Harbor closed"
  20 "Caution Area: Risk (define in associated text field)"
  21 "Caution Area: Underwater vehicle operation"
  22 "(reserved for future use)"
  23 "Environmental Caution Area: Storm front (line squall)"
  24 "Environmental Caution Area: Hazardous sea ice"
  25 "Environmental Caution Area: Storm warning (storm cell or line of storms)"
  26 "Environmental Caution Area: High wind"
  27 "Environmental Caution Area: High waves"
  28 "Environmental Caution Area: Restricted visibility (fog, rain, etc.)"
  29 "Environmental Caution Area: Strong currents"
  30 "Environmental Caution Area: Heavy icing"
  31 "(reserved for future use)"
  32 "Restricted Area: Fishing prohibited"
  33 "Restricted Area: No anchoring."
  34 "Restricted Area: Entry approval required prior to transit"
  35 "Restricted Area: Entry prohibited"
  36 "Restricted Area: Active military OPAREA"
  37 "Restricted Area: Firing – danger area."
  38 "Restricted Area: Drifting Mines"
  39 "(reserved for future use)"
  40 "Anchorage Area: Anchorage open"
  41 "Anchorage Area: Anchorage closed"
  42 "Anchorage Area: Anchorage prohibited"
  43 "Anchorage Area: Deep draft anchorage"
  44 "Anchorage Area: Shallow draft anchorage"
  45 "Anchorage Area: Vessel transfer operations"
  46 "(reserved for future use)"
  47 "(reserved for future use)"
  48 "(reserved for future use)"
  49 "(reserved for future use)"
  50 "(reserved for future use)"
  51 "(reserved for future use)"
  52 "(reserved for future use)"
  53 "(reserved for future use)"
  54 "(reserved for future use)"
  55 "(reserved for future use)"
  56 "Security Alert - Level 1"
  57 "Security Alert - Level 2"
  58 "Security Alert - Level 3"
  59 "(reserved for future use)"
  60 "(reserved for future use)"
  61 "(reserved for future use)"
  62 "(reserved for future use)"
  63 "(reserved for future use)"
  64 "Distress Area: Vessel disabled and adrift"
  65 "Distress Area: Vessel sinking"
  66 "Distress Area: Vessel abandoning ship"
  67 "Distress Area: Vessel requests medical assistance"
  68 "Distress Area: Vessel flooding"
  69 "Distress Area: Vessel fire/explosion"
  70 "Distress Area: Vessel grounding"
  71 "Distress Area: Vessel collision"
  72 "Distress Area: Vessel listing/capsizing"
  73 "Distress Area: Vessel under assault"
  74 "Distress Area: Person overboard"
  75 "Distress Area: SAR area"
  76 "Distress Area: Pollution response area"
  77 "(reserved for future use)"
  78 "(reserved for future use)"
  79 "(reserved for future use)"
  80 "Instruction: Contact VTS at this point/juncture"
  81 "Instruction: Contact Port Administration at this point/juncture"
  82 "Instruction: Do not proceed beyond this point/juncture"
  83 "Instruction: Await instructions prior to proceeding beyond this point/juncture"
  84 "Proceed to this location – await instructions"
  85 "Clearance granted – proceed to berth"
  86 "(reserved for future use)"
  87 "(reserved for future use)"
  88 "Information: Pilot boarding position"
  89 "Information: Icebreaker waiting area"
  90 "Information: Places of refuge"
  91 "Information: Position of icebreakers"
  92 "Information: Location of response units"
  93 "VTS active target"
  94 "Rogue or suspicious vessel"
  95 "Vessel requesting non-distress assistance"
  96 "Chart Feature: Sunken vessel"
  97 "Chart Feature: Submerged object"
  98 "Chart Feature: Semi-submerged object"
  99 "Chart Feature: Shoal area"
 100 "Chart Feature: Shoal area due north"
 101 "Chart Feature: Shoal area due east"
 102 "Chart Feature: Shoal area due south"
 103 "Chart Feature: Shoal area due west"
 104 "Chart Feature: Channel obstruction"
 105 "Chart Feature: Reduced vertical clearance"
 106 "Chart Feature: Bridge closed"
 107 "Chart Feature: Bridge partially open"
 108 "Chart Feature: Bridge fully open"
 109 "(reserved for future use)"
 110 "(reserved for future use)"
 111 "(reserved for future use)"
 112 "Report from ship: Icing info"
 113 "(reserved for future use)"
 114 "Report from ship: Miscellaneous information – define in associated text field"
 115 "(reserved for future use)"
 116 "(reserved for future use)"
 117 "(reserved for future use)"
 118 "(reserved for future use)"
 119 "(reserved for future use)"
 120 "Route: Recommended route"
 121 "Route: Alternative route"
 122 "Route: Recommended route through ice"
 123 "(reserved for future use)"
 124 "(reserved for future use)"
 125 "Other – Define in associated text field"
 126 "Cancellation – cancel area as identified by Message Linkage ID"
 127 "Undefined (default)"
})

(def mooring-position {
 0 "Not available (default)"
 1 "Port-side to"
 2 "Starboard-side to"
 3 "Mediterranean (end-on) mooring"
 4 "Mooring buoy"
 5 "Anchorage"
 6 "Reserved for future use"
 7 "Reserved for future use"
})

(def service-status {
 0 "Not available or requested (default)"
 1 "Service available"
 2 "No data or unknown"
 3 "Not to be used"
})

(def subarea-shape {
 0 "Circle or point"
 1 "Rectangle"
 2 "Sector"
 3 "Polyline"
 4 "Polygon"
 5 "Associated text"
 6 "Reserved"
 7 "Reserved"
})
