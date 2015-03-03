(ns ef-example.send-simple-event
  (:require
    [cheshire.core :as json]
    [clojure.tools.logging :as log]
    [event-fabric.api-client :as ef]
    [clojure.java.io :as io]))

(defn- ef-login [username password url]
  (let [client (ef/new-client username password url)]
    (prn "login on ef")
    (try (ef/login client)
         (catch Exception error (prn error)))))

(defn run-local! [config-path]
  (log/info "Running locally with config from" config-path)
  (let [config (json/parse-stream (clojure.java.io/reader config-path))
        folder (get config "folder")
        username (get config "ef-username")
        password (get config "ef-password")
        url (get config "ef-url")
        [login-ok session] (ef-login username password url)
        channel (get config "ef-channel")]
    (if login-ok
      (prn (ef/send-event session {:text "cpu" :percentage 10} channel))
      (prn "not logged" username password url))))

(defn -main
  ([]
   (run-local! "config.json"))
  ([config-path]
   (run-local! config-path)))
