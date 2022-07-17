(ns game.jvm
  (:require [nrepl.cmdline]
            [game.core]))

(defonce repl
  (delay
    (try
      (apply nrepl.cmdline/-main
             ["--middleware"
              "[\"refactor-nrepl.middleware/wrap-refactor\", \"cider.nrepl/cider-middleware\"]"])

      (catch Exception e
        (println e)))))

(defn register-methods
  [p-handle]
  (future @repl)
  (game.core/init! p-handle))
