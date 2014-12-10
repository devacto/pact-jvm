pact-jvm-consumer-clojure
==========================

Clojure DSL for Pact JVM

### Usage

Sample code to create pact file is below:
```clojure
(defpacttest sample-test
             (let [alice-consumer-fragment
                   (service-consumer "Consumer"
                                     (has-pact-with "Alice Service")
                                     (port 1234)
                                     (given "there is some good Mallory")
                                     (upon-receiving "a retrieve Mallory request")
                                     (with-attributes {:method "get"
                                                       :path   "/mallory"})
                                     (will-respond-with {:status  200
                                                         :headers {"Content-Type" "text/html"}
                                                         :body    "That is some good Mallory."}))
                   response (run alice-consumer-fragment
                                 (using @(http/get "http://test:1234/")))]
                  (is (= 200 (:status response)))
                  (is (= "text/html" (:content-type response)))
                  (is (= "That is some good Mallory." (:text (:data response))))))
```
