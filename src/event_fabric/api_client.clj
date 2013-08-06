(ns event-fabric.api-client
  (:require [cheshire.core :as json]
            [clj-http.client :as http]))

(def default-root-url "https://event-fabric.com/api/")

(defn- default-requester [url body cookies]
  (let [json-body (json/generate-string body)
        base-request-opts {:body json-body :content-type :json}
        request-opts (if cookies
                       (assoc base-request-opts :cookies cookies)
                       base-request-opts)]
    (http/post url request-opts)))

(defn- append-slash-if-missing [value]
  (if (.endsWith value "/")
    value
    (str value "/")))

(defn- make-session [client-data session-data]
  {:client-data client-data :session-data session-data})

(defn- make-endpoint [root-url endpoint-name]
  (str root-url endpoint-name))

(defn new-client [username password & [root-url]]
  {:username username
   :password password
   :root-url (append-slash-if-missing (or root-url default-root-url))})

(defn login [client & [requester]]
  (let [requester (or requester default-requester)
        {:keys [username password root-url]} client
        request-data {:username username :password password}
        service-url (make-endpoint root-url "session")
        response (requester service-url request-data)
        status (:status response)]
    (if (= status 200)
      [true (make-session client (:cookies response))]
      [false (make-session client nil)])))

(defn send-event [session value channel & [requester]]
  (let [requester (or requester default-requester)
        root-url (get-in session [:client-data :root-url])
        service-url (make-endpoint root-url "event")
        request-data {:value value :channel channel}
        cookies (:session-data session)
        response (requester service-url request-data cookies)
        status (:status response)]
    [(= status 201) response]))

