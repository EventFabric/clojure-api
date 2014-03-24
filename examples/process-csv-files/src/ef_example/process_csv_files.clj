(ns ef-example.process-csv-files
  (:require
    [clj-time.format :as time-fmt]
    [clj-time.coerce :as time-coerce]
    [cheshire.core :as json]
    [clojure.tools.logging :as log]
    [event-fabric.api-client :as ef]
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]))

(defn- generate-event [process-row]
  (let [[text value] process-row]
    {:text text
     :value value}))

(defn- ef-login [username password url]
  (let [client (ef/new-client username password url)]
    (prn "login on ef")
    (try (ef/login client)
         (catch Exception error (prn error)))))

(defn- make-ef-send-event [session channel]
  (fn [value]
    (prn "sending event to event fabric" channel value)
    ;true because it is a volatile event
    (ef/send-event session value channel true)))

(defn- read-file [file-path ef-send-event]
  (with-open [in-file (io/reader file-path)]
    (doseq [process-row (csv/read-csv in-file)]
      (try
        (ef-send-event (generate-event process-row))
        (catch Exception error (prn error process-row)))
      (Thread/sleep 1000))))

(defn- filter-files [files]
  (filter (fn [file]
            (let [file-name (.getName file)]
              (or (.endsWith file-name ".csv") (.endsWith file-name ".txt")))) files))

(defn- read-files-from-dir [dir-path ef-send-event]
  (let [directory (io/file dir-path)
        files (file-seq directory)
        allowed-files (filter-files files)]
    (prn "loading files from" dir-path "files" allowed-files)
    (doseq [file allowed-files]
      (read-file (.getAbsolutePath file) ef-send-event)
    (Thread/sleep 1000))))

(defn run-local! [config-path]
  (log/info "Running locally with config from" config-path)
  (let [config (json/parse-stream (clojure.java.io/reader config-path))
        folder (get config "folder")
        username (get config "ef-username")
        password (get config "ef-password")
        url (get config "ef-url")
        [login-ok session] (ef-login username password url)
        channel (get config "ef-channel")
        ef-send-event (make-ef-send-event session channel)]
    (if login-ok
    (read-files-from-dir folder ef-send-event)
      (prn "not logged" username password url))))

(defn -main
  ([]
   (run-local! "config.json"))
  ([config-path]
   (run-local! config-path)))
