(ns au.com.dius.pact.consumer.clojure.pact-clojure-test
  (:require [clojure.test :refer :all]
            [org.httpkit.client :as http]
            [au.com.dius.pact.consumer.clojure.pact-clojure :refer :all])
  (:import [au.com.dius.pact.consumer ConsumerPactBuilder ConsumerPactTest TestRun]
           [au.com.dius.pact.model MockProviderConfig]))

(deftest test-test
  (let [consumer-fragment (-> "test_consumer"
                              ConsumerPactBuilder/consumer
                              (.hasPactWith "test_provider")
                              (.uponReceiving "clojure test interaction")
                              (.path "/sample")
                              (.method "GET")
                              .willRespondWith
                              (.status 200)
                              .toFragment)
        config (MockProviderConfig. 23456 "localhost")]
    (is (= (ConsumerPactTest/PACT_VERIFIED)
           (.runConsumer consumer-fragment config
                         (proxy [TestRun] []
                           (run [_] (
                                      #(is (= 200
                                              (:status
                                                @(http/get (str (.url config) "/sample")))))))))))))
