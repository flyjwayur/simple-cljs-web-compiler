(ns cljs_compiler.core
  (:require
    [cljs.js :as cljs]))

;; -----------------------------------------------------------------------------
;; Compiler functions

(defn callback [{:keys [value error]}]
  (let [status (if error :error :ok)
        res (if error
              (.. error -cause -message)
              value)]
    [status res]))

(defn _compilation [s]
  (cljs/compile-str 
    (cljs/empty-state) 
    s
    callback))

(defn _eval [s]
  (cljs/eval-str 
    (cljs/empty-state) 
    s 
    'test 
    {:eval cljs/js-eval} 
    callback))

(defn _evaluation-js [s]
  (let [[status res] (_eval s)]
    [status (.stringify js/JSON res nil 4)]))

(defn _evaluation-clj [s]
  (let [[status res] (_eval s)]
    [status (str res)]))
