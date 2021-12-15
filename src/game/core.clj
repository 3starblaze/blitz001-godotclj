(ns game.core
  (:require
   [godotclj.api :as api :refer [->object]]
   [godotclj.bindings.godot :as godot]
   [godotclj.callbacks :as callbacks :refer [defer listen]]))

(defn main-ready [_]
  (println "Game has started"))

(defn main-process [_ _]
  nil)

(defn simplify-method [f]
  (fn [instance method_data user_data n-args p-args]
    (apply f (->object instance)
           (seq (godot/->indexed-variant-array n-args p-args)))))

(def classes
  {"StartMenu"
   {:base       "Panel"
    :create     (fn [& args] (println :create))
    :destroy    (fn [& args] (println :destroy))
    :methods    {"_ready" (simplify-method main-ready)
                 "_process" (simplify-method main-process)}}})

(defn register-methods [p-handle]
  (godot/register-classes p-handle classes)
  (apply callbacks/register-callbacks p-handle (keys classes)))
