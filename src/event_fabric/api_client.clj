(ns event-fabric.api-client
  (:require [cheshire.core :as json]
            [clj-http.client :as http]))

(def default-root-url "https://event-fabric.com/")

(defn- default-requester [url body & [token]]
  (let [json-body (json/generate-string body)
        base-request-opts {:body json-body :content-type :json}
        request-opts (if token
                       (assoc base-request-opts :headers {"x-session" token})
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
        service-url (make-endpoint root-url "sessions")
        response (requester service-url request-data)
        body (json/parse-string (:body response) true)
        token (:token body)
        status (:status response)]
    (if (= status 201)
      [true (make-session client token)]
      [false (make-session client nil)])))

(defn send-event [session value channel & [user requester]]
  (let [requester (or requester default-requester)
        root-url (get-in session [:client-data :root-url])
        username (or user (get-in session [:client-data :username]))
        stream-url (str "streams/" username "/" channel "/")
        service-url (make-endpoint root-url stream-url)
        token (:session-data session)
        response (requester service-url value token)
        status (:status response)]
    [(= status 201) response]))

