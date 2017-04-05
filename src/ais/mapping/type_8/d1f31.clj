(ns ais.mapping.type_8.d1f31
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def d1f31 (list
  {:len 25 :desc "Longitude"                 :tag "lon"          :fn common/mlon}
  {:len 24 :desc "Latitude"                  :tag "lat"          :fn common/mlat}
  {:len  1 :desc "Fix Quality"               :tag "accuracy"     :fn ais-types/b}
  {:len  5 :desc "Day"                       :tag "day"          :fn ais-types/u}
  {:len  5 :desc "Hour"                      :tag "hour"         :fn ais-types/u}
  {:len  6 :desc "Minute"                    :tag "minute"       :fn ais-types/u}
  {:len  7 :desc "Average Wind Speed"        :tag "wspeed"       :fn ais-types/u}
  {:len  7 :desc "Gust Speed"                :tag "wgust"        :fn ais-types/u}
  {:len  9 :desc "Wind Direction"            :tag "wdir"         :fn ais-types/u}
  {:len  9 :desc "Wind Gust Direction"       :tag "wgustdir"     :fn ais-types/u}
  {:len 11 :desc "Air Temperature"           :tag "airtemp"      :fn ais-types/I1}
  {:len  7 :desc "Relative Humidity"         :tag "humidity"     :fn ais-types/u}
  {:len 10 :desc "Dew Point"                 :tag "dewpoint"     :fn ais-types/I1}
  {:len  9 :desc "Air Pressure"              :tag "pressure"     :fn ais-types/u}
  {:len  2 :desc "Pressure Tendency"         :tag "pressuretend" :fn (partial ais-types/e ais-vocab/trend)}
  {:len  7 :desc "Max. Visibility"           :tag "visgreater"   :fn ais-types/b}
  {:len  8 :desc "Horizon Visibility"        :tag "visibility"   :fn ais-types/U1}
  {:len 12 :desc "Water Level"               :tag "waterlevel"   :fn ais-types/I2}
  {:len  2 :desc "Water Level Trend"         :tag "leveltrend"   :fn (partial ais-types/e ais-vocab/trend)}
  {:len  8 :desc "Surface Current Speed"     :tag "cspeed"       :fn ais-types/U1}
  {:len  9 :desc "Surface Current Direction" :tag "cdir"         :fn ais-types/u}
  {:len  8 :desc "Current Speed 2"           :tag "cspeed2"      :fn ais-types/U1}
  {:len  9 :desc "Current Direction 2"       :tag "cdir2"        :fn ais-types/u}
  {:len  5 :desc "Measurement Depth 2"       :tag "cdepth2"      :fn ais-types/U2}
  {:len  8 :desc "Current Speed 3"           :tag "cspeed3"      :fn ais-types/U1}
  {:len  9 :desc "Current Direction 3"       :tag "cdir3"        :fn ais-types/u}
  {:len  5 :desc "Measurement Depth 3"       :tag "cdepth3"      :fn ais-types/u}
  {:len  8 :desc "Wave Height"               :tag "waveheight"   :fn ais-types/U1}
  {:len  6 :desc "Wave Period"               :tag "waveperiod"   :fn ais-types/u}
  {:len  9 :desc "Wave Direction"            :tag "wavedir"      :fn ais-types/u}
  {:len  8 :desc "Swell Height"              :tag "swellheight"  :fn ais-types/U1}
  {:len  6 :desc "Swell Period"              :tag "swellperiod"  :fn ais-types/u}
  {:len  9 :desc "Swell Direction"           :tag "swelldir"     :fn ais-types/u}
  {:len  4 :desc "Sea State"                 :tag "seastate"     :fn (partial ais-types/e ais-vocab/beaufort-scale)}
  {:len 10 :desc "Water Temperature"         :tag "watertemp"    :fn ais-types/I1}
  {:len  3 :desc "Precipitation"             :tag "preciptype"   :fn (partial ais-types/e ais-vocab/precipitation-type)}
  {:len  9 :desc "Salinity"                  :tag "salinity"     :fn ais-types/U1}
  {:len  2 :desc "Ice"                       :tag "ice"          :fn ais-types/u}
  {:len 10 :desc "Spare"                     :tag "spare"        :fn ais-types/x}
))
