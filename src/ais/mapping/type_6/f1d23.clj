(ns ais.mapping.type_6.f1d23
  (:require [ais.vocab :as ais-vocab]
            [ais.types :as ais-types]
            [ais.mapping.common :as common])
  (:gen-class))


(def circle_or_point (list
  {:len   3 :desc "Shape of area" :tag "shape"     :fn (partial ais-types/e ais-vocab/subarea-shape)}
  {:len   2 :desc "Scale Factor"  :tag "scale"     :fn ais-types/u}
  {:len  25 :desc "Longitude"     :tag "lon"       :fn common/slon}
  {:len  24 :desc "Latitude"      :tag "lat"       :fn common/slat}
  {:len   3 :desc "Precision"     :tag "precision" :fn ais-types/u}
  {:len  12 :desc "Radius"        :tag "radius"    :fn ais-types/u}
  {:len  18 :desc "Spare"         :tag "spare"     :fn ais-types/x}
))

(def rectangle (list
  {:len   3 :desc "Shape of area" :tag "shape"       :fn (partial ais-types/e ais-vocab/subarea-shape)}
  {:len   2 :desc "Scale Factor"  :tag "scale"       :fn ais-types/u}
  {:len  25 :desc "Longitude"     :tag "lon"         :fn common/slon}
  {:len  24 :desc "Latitude"      :tag "lat"         :fn common/slat}
  {:len   3 :desc "Precision"     :tag "precision"   :fn ais-types/u}
  {:len   8 :desc "E dimension"   :tag "east"        :fn ais-types/u}
  {:len   8 :desc "N dimension"   :tag "north"       :fn ais-types/u}
  {:len   9 :desc "Orientation"   :tag "orientation" :fn ais-types/u}
  {:len   5 :desc "Spare"         :tag "spare"       :fn ais-types/x}
))

(def sector (list
  {:len   3 :desc "Shape of area"  :tag "shape"     :fn (partial ais-types/e ais-vocab/subarea-shape)}
  {:len   2 :desc "Scale Factor"   :tag "scale"     :fn ais-types/u}
  {:len  25 :desc "Longitude"      :tag "lon"       :fn common/slon}
  {:len  24 :desc "Latitude"       :tag "lat"       :fn common/slat}
  {:len   3 :desc "Precision"      :tag "precision" :fn ais-types/u}
  {:len  12 :desc "Radius"         :tag "radius"    :fn ais-types/u}
  {:len   9 :desc "Left Boudnary"  :tag "left"      :fn ais-types/u}
  {:len   9 :desc "Right Boundary" :tag "right"     :fn ais-types/u}
))


;;
;; *** SUB ARRAY DEFINTIONS ***
;;

(def bearing-distance (list
  {:len 10 :desc "Bearing"  :tag "bearing"  :fn ais-types/u}
  {:len 10 :desc "Distance" :tag "distance" :fn ais-types/u}
))

(def sub-bits-len (partial ais-types/array-bit-len 4 80))

(defn sub-mapper [_]
  bearing-distance)

(def sub-handler (partial ais-types/a 80 sub-mapper common/parse-binary))

;;
;; Waypoints
;;
(def polyline (list
  {:len            3 :desc "Shape of area" :tag "shape"     :fn (partial ais-types/e ais-vocab/subarea-shape)}
  {:len            2 :desc "Scale Factor"  :tag "scale"     :fn ais-types/u}
  {:len sub-bits-len :desc "Waypoints"     :tag "waypoints" :fn sub-handler :a true}
  {:len            2 :desc "Spare"         :tag "spare"     :fn ais-types/x}
))

;;
;; Vertices
;;
(def polygon (list
  {:len            3 :desc "Shape of area" :tag "shape"    :fn (partial ais-types/e ais-vocab/subarea-shape)}
  {:len            2 :desc "Scale Factor"  :tag "scale"    :fn ais-types/u}
  {:len sub-bits-len :desc "Vertices"      :tag "vertices" :fn sub-handler :a true}
  {:len            2 :desc "Spare"         :tag "spare"    :fn ais-types/x}
))

(def associated-text (list
  {:len  3 :desc "Shape of area" :tag "shape" :fn (partial ais-types/e ais-vocab/subarea-shape)}
  {:len 84 :desc "Text"          :tag "text"  :fn (partial ais-types/t ais-vocab/sixbit-ascii 14)}
))

(defn field-mapper [bits]
  (let [map-id (ais-types/u nil (subs bits 0 3))]
    (condp = map-id
      0 circle_or_point
      1 rectangle
      2 sector
      3 polyline
      4 polygon
      5 associated-text)))

(def bits-len (partial ais-types/array-bit-len 10 87))

(def seq-handler (partial ais-types/a 87 field-mapper common/parse-binary))

(def f1d23 (list
  {:len       10 :desc "Message Linkage ID" :tag "linkage"  :fn ais-types/u}
  {:len        7 :desc "Notice Description" :tag "notice"   :fn (partial ais-types/e ais-vocab/area-notice-description)}
  {:len        4 :desc "Month (UTC)"        :tag "month"    :fn ais-types/u}
  {:len        5 :desc "Day (UTC)"          :tag "day"      :fn ais-types/u}
  {:len        5 :desc "Hour (UTC)"         :tag "hour"     :fn ais-types/u}
  {:len        6 :desc "Minute (UTC)"       :tag "minute"   :fn ais-types/u}
  {:len       18 :desc "Duration"           :tag "duration" :fn ais-types/u}
  {:len bits-len :desc "Subareas"           :tag "subareas" :fn seq-handler :a true}
))
