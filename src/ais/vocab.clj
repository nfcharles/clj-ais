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
