(ns cljs_compiler.core
  (:require
    [cljs.js :as cljs]
    [goog.dom :as gdom]
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]))

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

;; -----------------------------------------------------------------------------

(defonce app-state (atom
  {:input ""
   :compilation "" 
   :evaluation-js "" 
   :evaluation-clj ""}))

;; -----------------------------------------------------------------------------
;; Parsing

(defn read [{:keys [state]} key params]
  {:value (get @state key "")})

(defmulti mutate om/dispatch)

(defmethod mutate 'input/save [{:keys [state]} _ {:keys [value]}]
  {:action (fn [] 
            (swap! state assoc :input value))})

(defmethod mutate 'cljs/compile [{:keys [state]} _ {:keys [value]}]
  {:action (fn [] 
            (swap! state update :compilation 
              (partial _compilation value)))})

(defmethod mutate 'js/eval [{:keys [state]} _ {:keys [value]}]
  {:action (fn [] 
            (swap! state update :evaluation-js 
              (partial _evaluation-js value)))})

(defmethod mutate 'clj/eval [{:keys [state]} _ {:keys [value]}]
  {:action (fn [] 
            (swap! state update :evaluation-clj 
              (partial _evaluation-clj value)))})

(def parser (om/parser {:read read 
                        :mutate mutate}))

(def reconciler 
  (om/reconciler 
    {:state app-state 
     :parser parser}))

(defn process-input [compiler s]
  (om/transact! compiler 
       [(list 'input/save     {:value s})
        (list 'cljs/compile   {:value s})
        (list 'js/eval        {:value s})
        (list 'clj/eval       {:value s})]))

;; -----------------------------------------------------------------------------
;; UI Component

(defn input-ui [reconciler]
  (dom/section nil
    (dom/textarea #js {:autoFocus true
                       :onChange #(process-input 
                                    reconciler
                                    (.. % -target -value))})))

(defn compile-cljs-ui [{:keys [compilation]}]
  (let [[status result] compilation]
    (dom/section nil
                 (dom/textarea #js {:value result
                                    :readOnly true}))))

(defn evaluate-clj-ui [{:keys [evaluation-clj]}]
  (let [[status result] evaluation-clj]
    (dom/section nil
                 (dom/textarea #js {:value result
                                    :readOnly true}))))

(defn evaluate-js-ui [{:keys [evaluation-js]}]
  (let [[status result] evaluation-js]
    (dom/section nil
                 (dom/textarea #js {:value result
                                    :readOnly true}))))

(defui CompilerUI
  
  static om/IQuery
  (query [this] 
    '[:compilation :evaluation-js :evaluation-clj])
  
  Object
  (render [this]
    (as->
      (om/props this) $
      (dom/div nil
        (input-ui this)
        (compile-cljs-ui $)
        (evaluate-clj-ui $)
        (evaluate-js-ui $)))))

;; -----------------------------------------------------------------------------

(om/add-root! reconciler
              CompilerUI (gdom/getElement "app"))
