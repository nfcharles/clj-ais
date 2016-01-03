(ns ais.macro
  (:gen-class))

; Macro from 
; https://github.com/markmandel/while-let/blob/master/src/while_let/core.clj

(defmacro while-let
  "Repeatedly executes body while test expression is true, evaluating the body with binding-form bound to the value of test."
  [[form test] & body]
    `(loop [~form ~test]
      (when ~form
        ~@body
        (recur ~test))))