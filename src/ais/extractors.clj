(ns ais.extractors
  (:gen-class))

;;;
;;; ----- MESSAGE SYNTAX -----
;;;
;;; \g:1-2-73874,n:157036,s:r003669945,c:1241544035*4A\!AIVDM,1,1,,B,15N4cJ`005Jrek0H@9n`DW5608EP,0*13
;;; 
;;; Tag Block
;;; ---------
;;; \g:(1),n:(2),c:(3),s:(4)*(5)\
;;; 
;;; 1: Sentence grouping
;;; 2: Line count
;;; 3: Unix time in seconds or milliseconds
;;; 4: Source / Station
;;; 5: Tag block checksum
;;;
;;; Message Envelope
;;; ----------------
;;; !(1),(2),(3),(4),(5),(6),(7)*(8)
;;;
;;; 1: Packet type
;;; 2: Count of fragments    (> 1 for multi-part message)
;;; 3: Fragment number       (1..n)
;;; 4: Sequential message ID (applicable to multi-part messages)
;;; 5: Radio channel code    (A | B)
;;; 6: Data payload
;;; 7: Number of fill bits
;;; 8: Envelope checksum

(defn- ext-raw [regex msg]
   (nth (re-find regex msg) 1))

(defn- ext [regex msg]
  (if-let [ret (ext-raw regex msg)]
    (read-string ret) nil))

(defn- ext-multi-raw [regex msg]
  (rest (re-find regex msg)))

(defn- ext-multi [regex msg]
  (map read-string (ext-multi-raw regex msg)))

(def -extractors (hash-map
 "g"          { :exp #"\\.*g:\d-(\d-\d+).*\\"                                  :fn #(ext-raw %1 %2) }
 "n"          { :exp #"\\.*n:(\d*).*\\"                                        :fn #(ext %1 %2) }
 "c"          { :exp #"\\.*c:(\d*).*\\"                                        :fn #(ext %1 %2) }
 "t"          { :exp #"\\.*t:(\d*).*\\"                                        :fn #(ext %1 %2) }
 "s"          { :exp #"\\.*s:(\w*).*\\"                                        :fn #(ext-raw %1 %2) }
 "tags"       { :exp #"^(\\.+\\)"                                              :fn #(ext-raw %1 %2) }
 "pac-type"   { :exp #"(AIVD[MO])"                                             :fn #(ext-raw %1 %2) }
 "frag-count" { :exp #"AIVD[MO],(\d)"                                          :fn #(ext %1 %2) }
 "frag-num"   { :exp #"AIVD[MO],\d,(\d)"                                       :fn #(ext %1 %2) }
 "frag-info"  { :exp #"AIVD[MO],(\d),(\d)"                                     :fn #(ext-multi %1 %2) }
 "seq-id"     { :exp #"AIVD[MO],\d,\d,(\d?)"                                   :fn #(ext-raw %1 %2) }
 "radio-ch"   { :exp #"AIVD[MO],\d,\d,\d?,([AB12]?)"                           :fn #(ext-raw %1 %2) }
 "payload"    { :exp #"AIVD[MO],\d,\d,\d?,[AB12]?,(.+),"                       :fn #(ext-raw %1 %2) }
 "fill-bits"  { :exp #"AIVD[MO],\d,\d,\d?,[AB12]?,.+,(\d)"                     :fn #(ext %1 %2) }
 "checksum"   { :exp #"AIVD[MO],\d,\d,\d?,[AB12]?,.+,\d\*([A-F0-9][A-F0-9])"   :fn #(ext-raw %1 %2) }
 "env"        { :exp #"(AIVD[MO],\d,\d,\d?,[AB12]?,.+,\d)"                     :fn #(ext-raw %1 %2) }
 "env-chksum" { :exp #"(AIVD[MO],\d,\d,\d?,[AB12]?,.+,\d)\*([A-Z0-9][A-Z0-9])" :fn #(ext-multi-raw %1 %2) }
 "sentence"   { :exp #"(AIVD[MO],\d,\d,\d?,[AB12]?,.+,\d\*[A-Z0-9][A-Z0-9])"   :fn #(ext-raw %1 %2) }))

(defn parse [field msg]
  (let [ex (-extractors field)]
    ((ex :fn) (ex :exp) msg)))

(defn tokenize [line]
  (if-let [sentence (parse "sentence" line)]
    (if-let [tokens (clojure.string/split sentence #"[,*]")]
      (if (= 8 (count tokens))
        (hash-map
          :tg (parse "tags" line)
          :en (parse "env" line)
          :fc (read-string (nth tokens 1))
          :fn (read-string (nth tokens 2))
          :pl (nth tokens 5)
          :fl (read-string (nth tokens 6))
          :ck (nth tokens 7))))))
