pact-jvm-consumer-clojure
==========================

Clojure DSL for Pact JVM

### Usage

Sample code to create pact file is below:

def alice_service = new PactBuilder() // Create a new PactBuilder
        alice_service {
            serviceConsumer "Consumer"  // Define the service consumer by name
            hasPactWith "Alice Service"   // Define the service provider that it has a pact with
            port 1234                       // The port number for the service. It is optional, leave it out to
                                            // to use a random one

            given('there is some good mallory') // defines a provider state. It is optional.
            uponReceiving('a retrieve Mallory request') // upon_receiving starts a new interaction
            withAttributes(method: 'get', path: '/mallory')     // define the request, a GET request to '/mallory'
            willRespondWith(                        // define the response we want returned
                status: 200,
                headers: ['Content-Type': 'text/html'],
                body: '"That is some good Mallory."'
            )
        }

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
