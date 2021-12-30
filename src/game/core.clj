(ns game.core
  (:require
   [godotclj.api :as api :refer [->object]]
   [godotclj.bindings.godot :as godot]
   [godotclj.core]))

(def Input (->object "Input"))

(def player-velocity 300) ;; px/s
(def gravity 1000) ;; px/s^2
(def jump-velocity 500)
(def coyote-time 0.02)
(def max-level-count 3)

(def current-velocity (atom [0 0]))
(def coyote-time-counter (atom 0))
(def current-level (atom 1))

(defn- get-x-direction []
  (- (.getActionStrength Input "ui_right")
     (.getActionStrength Input "ui_left")))

(defn can-jump? [body]
  (or (.isOnFloor body)
      (< @coyote-time-counter coyote-time)))

(defn set-current-velocity [x y]
  (swap! current-velocity #(vector (or x (first %)) (or y (second %)))))

(defn physics-process [level delta]
  (let [player (.getNode level "Player")]
    (set-current-velocity (* (get-x-direction) player-velocity) nil)
    (if (.isOnFloor player)
      (reset! coyote-time-counter 0)
      (swap! coyote-time-counter + delta))

    (set-current-velocity
     nil
     (if (and (.isActionJustPressed Input "ui_up")
              (can-jump? player))
       (- jump-velocity)
       (+ (second @current-velocity)
          (* gravity delta))))
    (.moveAndSlide player (api/vec2 @current-velocity) (api/vec2 [0 -1]))))

(defn get-root []
  (.getRoot (.getMainLoop (->object "_Engine"))))

(defn load-level [n]
  (.changeScene (.getTree (get-root))
                (format "res://scenes/levels/Level%s.tscn" n)))

(defn on-start-button-pressed [this]
  (load-level 1))

(defn main-ready [this]
  (println "Game has started")
  (.connect (.getNode this "CenterContainer/StartButton")
            "pressed"
            this
            "_onStartButtonPressed"))

(defn on-finish-flag-area-entered [_ _]
  (println "Level has been completed!")
  (if (<= (inc @current-level) max-level-count)
    (do (swap! current-level inc)
        (load-level @current-level))
    (println "All levels have been completed!")))

(defn level-ready [this]
  (.connect (.getNode this "FinishFlag")
            "body_entered"
            this
            "_onFinishFlagAreaEntered"))

(defn simplify-method [f]
  (fn [instance method_data user_data n-args p-args]
    (apply f (->object instance)
           (seq (godot/->indexed-variant-array n-args p-args)))))

(def register-methods
  (godotclj.core/gen-register-fn
   {"StartMenu"
    {:methods    {"_ready" (simplify-method main-ready)
                  "_onStartButtonPressed"
                  (simplify-method on-start-button-pressed)}}
    "BaseLevel"
    {:methods {"_ready" (simplify-method level-ready)
               "_physics_process" (simplify-method physics-process)
               "_onFinishFlagAreaEntered"
               (simplify-method on-finish-flag-area-entered)}}}))
