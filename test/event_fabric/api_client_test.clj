(ns event-fabric.api-client-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as json])
  (:use event-fabric.api-client))

(defn fake-response [status]
  {:status status})

(defn fake-requester [storage response-status]
  (fn [url body]
    (swap! storage #(conj % {:url url
                             :body body}))
    {:status response-status}))

(deftest client-test
  (testing "a client can be constructed"
    (let [{:keys [username password root-url]} (new-client "username"
                                                            "password")]
      (is (= username "username"))
      (is (= password "password"))
      (is (= root-url default-root-url))))

  (testing "a client can be fully constructed without ending slash"
    (let [url "http://localhost:8080/ef/api"
          {:keys [username password root-url]} (new-client "username"
                                                           "password" url)]
      (is (= username "username"))
      (is (= password "password"))
      (is (= root-url (str url "/")))))

  (testing "a client can be fully constructed with ending slash"
    (let [url "http://localhost:8080/ef/api/"
          {:keys [username password root-url]} (new-client "username"
                                                           "password" url)]
      (is (= username "username"))
      (is (= password "password"))
      (is (= root-url url))))
  
  (testing "login works"
    (let [storage (atom [])
          req-url "http://localhost:8080/"
          client (new-client "username" "password" req-url)
          requester (fake-requester storage 200)
          [login-ok session] (login client requester)
          {:keys [url body]} (first @storage)]

      (is login-ok)
      (is (= url (str req-url "sessions")))
      (is (= body {:username "username" :password "password"}))))

  (testing "send event works"
    (let [login-storage (atom [])
          req-url "http://localhost:8080/"
          client (new-client "username" "password" req-url)
          login-requester (fake-requester login-storage 200)
          [login-ok session] (login client login-requester)

          send-storage (atom [])
          send-requester (fake-requester send-storage 201)
          [send-ok response] (send-event session {:name "bob" :count 11}
                                         "my.channel" send-requester)
          {:keys [url body]} (first @send-storage)]


      (is send-ok)
      (is (= url (str req-url "/streams")))
      (is (= body {:channel "my.channel" :value {:name "bob" :count 11}}))))
  )
